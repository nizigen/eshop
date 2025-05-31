package com.example.eshop.service.impl;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.Product;
import com.example.eshop.model.ShoppingCartItem;
import com.example.eshop.model.User;
import com.example.eshop.repository.ProductRepository;
import com.example.eshop.repository.ShoppingCartRepository;
import com.example.eshop.service.ShoppingCartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

  private static final Logger log = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

  @Autowired
  private ShoppingCartRepository cartRepository;

  @Autowired
  private ProductRepository productRepository;

  @Override
  @Transactional
  public ShoppingCartItem addToCart(Long productId, Integer quantity, User user, String sessionId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    // 检查库存
    if (product.getQuantity() < quantity) {
      throw new IllegalArgumentException("商品库存不足");
    }

    Optional<ShoppingCartItem> existingItem;
    if (user != null) {
      existingItem = cartRepository.findByUserAndProductId(user, productId);
    } else {
      existingItem = cartRepository.findBySessionIdAndProductId(sessionId, productId);
    }

    if (existingItem.isPresent()) {
      ShoppingCartItem item = existingItem.get();
      int newQuantity = item.getQuantity() + quantity;
      // 检查更新后的总数量是否超过库存
      if (newQuantity > product.getQuantity()) {
        throw new IllegalArgumentException("商品库存不足");
      }
      item.setQuantity(newQuantity);
      return cartRepository.save(item);
    } else {
      ShoppingCartItem newItem = new ShoppingCartItem();
      newItem.setProduct(product);
      newItem.setQuantity(quantity);
      if (user != null) {
        newItem.setUser(user);
      } else {
        newItem.setSessionId(sessionId);
      }
      return cartRepository.save(newItem);
    }
  }

  @Override
  @Transactional
  public ShoppingCartItem updateQuantity(Long cartItemId, Integer quantity, User user, String sessionId) {
    log.info("开始更新购物车数量: cartItemId={}, quantity={}, user={}, sessionId={}",
        cartItemId, quantity, user != null ? user.getId() : null, sessionId);

    // 查找购物车项
    ShoppingCartItem item = findCartItem(cartItemId, user, sessionId);
    if (item == null) {
      log.error("购物车项不存在: cartItemId={}", cartItemId);
      throw new ResourceNotFoundException("购物车项不存在");
    }

    // 检查商品是否存在
    Product product = item.getProduct();
    if (product == null) {
      log.error("商品不存在: cartItemId={}", cartItemId);
      throw new ResourceNotFoundException("商品不存在");
    }

    // 检查库存
    if (quantity > product.getQuantity()) {
      log.warn("商品库存不足: productId={}, requested={}, available={}",
          product.getId(), quantity, product.getQuantity());
      throw new IllegalArgumentException("商品库存不足");
    }

    // 检查数量是否合法
    if (quantity < 1) {
      log.warn("商品数量不合法: cartItemId={}, quantity={}", cartItemId, quantity);
      throw new IllegalArgumentException("商品数量必须大于0");
    }

    // 记录更新前的数量
    int oldQuantity = item.getQuantity();
    log.info("更新前数量: cartItemId={}, oldQuantity={}, newQuantity={}",
        cartItemId, oldQuantity, quantity);

    // 更新数量
    item.setQuantity(quantity);

    // 保存更新
    try {
      ShoppingCartItem savedItem = cartRepository.save(item);
      log.info("购物车数量更新成功: itemId={}, oldQuantity={}, newQuantity={}",
          savedItem.getId(), oldQuantity, savedItem.getQuantity());
      return savedItem;
    } catch (Exception e) {
      log.error("更新购物车数量失败: itemId={}, quantity={}, error={}",
          cartItemId, quantity, e.getMessage(), e);
      throw new RuntimeException("更新购物车数量失败: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public void removeFromCart(Long cartItemId, User user, String sessionId) {
    ShoppingCartItem item = findCartItem(cartItemId, user, sessionId);
    cartRepository.delete(item);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShoppingCartItem> getCartItems(User user, String sessionId) {
    if (user != null) {
      return cartRepository.findByUser(user);
    } else {
      return cartRepository.findBySessionId(sessionId);
    }
  }

  @Override
  @Transactional
  public void clearCart(User user, String sessionId) {
    if (user != null) {
      cartRepository.deleteByUser(user);
    } else {
      cartRepository.deleteBySessionId(sessionId);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public int getCartItemCount(User user, String sessionId) {
    if (user != null) {
      return cartRepository.sumQuantitiesByUser(user);
    } else {
      return cartRepository.sumQuantitiesBySessionId(sessionId);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal getCartTotal(User user, String sessionId) {
    List<ShoppingCartItem> items = getCartItems(user, sessionId);
    return items.stream()
        .map(ShoppingCartItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isProductInCart(Long productId, User user, String sessionId) {
    if (user != null) {
      return cartRepository.findByUserAndProductId(user, productId).isPresent();
    } else {
      return cartRepository.findBySessionIdAndProductId(sessionId, productId).isPresent();
    }
  }

  @Override
  @Transactional
  public void mergeAnonymousCart(String sessionId, User user) {
    if (!StringUtils.hasText(sessionId)) {
      return;
    }

    List<ShoppingCartItem> anonymousItems = cartRepository.findBySessionId(sessionId);
    for (ShoppingCartItem anonymousItem : anonymousItems) {
      Optional<ShoppingCartItem> existingItem = cartRepository.findByUserAndProductId(user,
          anonymousItem.getProduct().getId());

      if (existingItem.isPresent()) {
        // 如果用户购物车中已存在该商品，合并数量
        ShoppingCartItem userItem = existingItem.get();
        userItem.setQuantity(userItem.getQuantity() + anonymousItem.getQuantity());
        cartRepository.save(userItem);
      } else {
        // 如果用户购物车中不存在该商品，转移所有权
        anonymousItem.setUser(user);
        anonymousItem.setSessionId(null);
        cartRepository.save(anonymousItem);
      }
    }
  }

  private ShoppingCartItem findCartItem(Long cartItemId, User user, String sessionId) {
    ShoppingCartItem item = cartRepository.findById(cartItemId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

    // 验证购物车项属于当前用户或会话
    if (user != null && !user.equals(item.getUser())) {
      throw new ResourceNotFoundException("Cart item not found");
    } else if (user == null && !sessionId.equals(item.getSessionId())) {
      throw new ResourceNotFoundException("Cart item not found");
    }

    return item;
  }
}
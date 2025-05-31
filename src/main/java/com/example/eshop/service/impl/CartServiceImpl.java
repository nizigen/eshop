package com.example.eshop.service.impl;

import com.example.eshop.dto.CartItemDTO;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.CartItem;
import com.example.eshop.model.Product;
import com.example.eshop.model.User;
import com.example.eshop.repository.CartItemRepository;
import com.example.eshop.repository.ProductRepository;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final HttpSession httpSession;
  private static final String CART_SESSION_KEY = "anonymousCart";

  @Override
  @Transactional
  public CartItem addToCart(User user, CartItemDTO cartItemDTO) {
    Product product = productRepository.findById(cartItemDTO.getProductId())
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    if (product.getQuantity() < cartItemDTO.getQuantity()) {
      throw new IllegalArgumentException("Not enough stock available");
    }

    Optional<CartItem> existingItem = cartItemRepository.findByUserAndProductId(user, product.getId());

    if (existingItem.isPresent()) {
      CartItem item = existingItem.get();
      int newQuantity = item.getQuantity() + cartItemDTO.getQuantity();
      if (newQuantity > product.getQuantity()) {
        throw new IllegalArgumentException("Not enough stock available");
      }
      item.setQuantity(newQuantity);
      return cartItemRepository.save(item);
    } else {
      CartItem cartItem = new CartItem();
      cartItem.setUser(user);
      cartItem.setProduct(product);
      cartItem.setQuantity(cartItemDTO.getQuantity());
      return cartItemRepository.save(cartItem);
    }
  }

  @Override
  @Transactional
  public void removeFromCart(User user, Long productId) {
    cartItemRepository.deleteByUserAndProductId(user, productId);
  }

  @Override
  @Transactional
  public CartItem updateCartItem(User user, Long productId, Integer quantity) {
    CartItem cartItem = cartItemRepository.findByUserAndProductId(user, productId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

    Product product = cartItem.getProduct();
    if (quantity > product.getQuantity()) {
      throw new IllegalArgumentException("Not enough stock available");
    }

    cartItem.setQuantity(quantity);
    return cartItemRepository.save(cartItem);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CartItem> getCartItems(User user) {
    return cartItemRepository.findByUser(user);
  }

  @Override
  @Transactional
  public void clearCart(User user) {
    cartItemRepository.deleteByUser(user);
  }

  @Override
  public void addToCartAnonymous(CartItemDTO cartItem) {

    throw new UnsupportedOperationException("Anonymous cart not implemented yet");
  }

  @Override
  public void removeFromCartAnonymous(Long productId) {

    throw new UnsupportedOperationException("Anonymous cart not implemented yet");
  }

  @Override
  public List<CartItemDTO> getAnonymousCart() {

    throw new UnsupportedOperationException("Anonymous cart not implemented yet");
  }

  @Override
  public void clearAnonymousCart() {

    throw new UnsupportedOperationException("Anonymous cart not implemented yet");
  }

  @Override
  @Transactional
  public void mergeAnonymousCartWithUserCart(User user) {

    throw new UnsupportedOperationException("Anonymous cart not implemented yet");
  }

  @Override
  public double calculateTotalPrice(List<CartItem> cartItems) {
    return cartItems.stream()
        .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
        .sum();
  }

  @Override
  public int getTotalItems(List<CartItem> cartItems) {
    return cartItems.stream()
        .mapToInt(CartItem::getQuantity)
        .sum();
  }

  @Override
  public Optional<CartItem> getCartItem(Long id, User user) {
    return cartItemRepository.findById(id)
        .filter(item -> item.getUser().equals(user));
  }

  @Override
  public Optional<CartItem> getCartItemByProduct(Product product, User user) {
    return cartItemRepository.findByUserAndProductId(user, product.getId());
  }
}
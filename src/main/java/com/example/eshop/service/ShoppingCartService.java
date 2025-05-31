package com.example.eshop.service;

import com.example.eshop.model.ShoppingCartItem;
import com.example.eshop.model.User;
import java.util.List;
import java.math.BigDecimal;

public interface ShoppingCartService {
  /**
   * 添加商品到购物车（支持登录用户和匿名用户）
   */
  ShoppingCartItem addToCart(Long productId, Integer quantity, User user, String sessionId);

  /**
   * 更新购物车商品数量（支持登录用户和匿名用户）
   */
  ShoppingCartItem updateQuantity(Long cartItemId, Integer quantity, User user, String sessionId);

  /**
   * 从购物车移除商品（支持登录用户和匿名用户）
   */
  void removeFromCart(Long cartItemId, User user, String sessionId);

  /**
   * 获取用户的购物车商品列表（支持登录用户和匿名用户）
   */
  List<ShoppingCartItem> getCartItems(User user, String sessionId);

  /**
   * 清空购物车（支持登录用户和匿名用户）
   */
  void clearCart(User user, String sessionId);

  /**
   * 获取购物车中的商品总数（支持登录用户和匿名用户）
   */
  int getCartItemCount(User user, String sessionId);

  /**
   * 计算购物车商品总价（支持登录用户和匿名用户）
   */
  BigDecimal getCartTotal(User user, String sessionId);

  /**
   * 检查商品是否已在购物车中（支持登录用户和匿名用户）
   */
  boolean isProductInCart(Long productId, User user, String sessionId);

  /**
   * 将匿名购物车合并到用户购物车（用户登录时调用）
   */
  void mergeAnonymousCart(String sessionId, User user);
}
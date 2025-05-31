package com.example.eshop.service;

import com.example.eshop.dto.CartItemDTO;
import com.example.eshop.model.CartItem;
import com.example.eshop.model.Product;
import com.example.eshop.model.User;
import java.util.List;
import java.util.Optional;

public interface CartService {
  // User cart operations
  List<CartItem> getCartItems(User user);

  CartItem addToCart(User user, CartItemDTO cartItemDTO);

  void removeFromCart(User user, Long productId);

  CartItem updateCartItem(User user, Long productId, Integer quantity);

  void clearCart(User user);

  // Anonymous cart operations
  void addToCartAnonymous(CartItemDTO cartItem);

  void removeFromCartAnonymous(Long productId);

  List<CartItemDTO> getAnonymousCart();

  void clearAnonymousCart();

  void mergeAnonymousCartWithUserCart(User user);

  // Cart utilities
  double calculateTotalPrice(List<CartItem> cartItems);

  int getTotalItems(List<CartItem> cartItems);

  // Additional methods
  Optional<CartItem> getCartItem(Long id, User user);

  Optional<CartItem> getCartItemByProduct(Product product, User user);
}
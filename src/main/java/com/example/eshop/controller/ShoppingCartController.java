package com.example.eshop.controller;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.ShoppingCartItem;
import com.example.eshop.model.User;
import com.example.eshop.service.ShoppingCartService;
import com.example.eshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class ShoppingCartController {

  private static final Logger log = LoggerFactory.getLogger(ShoppingCartController.class);
  private final ShoppingCartService shoppingCartService;
  private final UserService userService;
  private static final String ANONYMOUS_CART_SESSION_KEY = "anonymousCartId";

  @GetMapping
  public String viewCart(Model model, Authentication authentication, HttpSession session) {
    String sessionId = getSessionId(session);
    User user = authentication != null ? userService.findByEmail(authentication.getName()).orElse(null) : null;

    List<ShoppingCartItem> cartItems = shoppingCartService.getCartItems(user, sessionId);
    model.addAttribute("cartItems", cartItems);
    model.addAttribute("total", shoppingCartService.getCartTotal(user, sessionId));
    model.addAttribute("itemCount", shoppingCartService.getCartItemCount(user, sessionId));

    return "cart/view";
  }

  @PostMapping("/add")
  @ResponseBody
  public Map<String, Object> addToCart(@RequestParam Long productId,
      @RequestParam(defaultValue = "1") Integer quantity,
      Authentication authentication,
      HttpSession session) {
    String sessionId = getSessionId(session);
    User user = authentication != null ? userService.findByEmail(authentication.getName()).orElse(null) : null;
    try {
      shoppingCartService.addToCart(productId, quantity, user, sessionId);
      return Map.of("success", true);
    } catch (Exception e) {
      return Map.of("success", false, "message", e.getMessage());
    }
  }

  @GetMapping("/add")
  public String redirectToCart() {
    return "redirect:/cart";
  }

  @PostMapping("/update/{cartItemId}")
  @ResponseBody
  public String updateQuantity(@PathVariable("cartItemId") Long cartItemId,
      @RequestParam("quantity") Integer quantity,
      Authentication authentication,
      HttpSession session) {
    log.info("Received update request for cartItemId: {}, quantity: {}", cartItemId, quantity);

    String sessionId = getSessionId(session);
    User user = authentication != null ? userService.findByEmail(authentication.getName()).orElse(null) : null;

    try {
      if (quantity == null || quantity < 1) {
        return "error: 商品数量必须大于0";
      }

      ShoppingCartItem updatedItem = shoppingCartService.updateQuantity(cartItemId, quantity, user, sessionId);
      if (updatedItem != null) {
        log.info("Successfully updated cart item: {}", updatedItem.getId());
        return "success";
      } else {
        log.error("Failed to update cart item: {}", cartItemId);
        return "error: 更新失败";
      }
    } catch (ResourceNotFoundException e) {
      log.error("Cart item not found: {}", cartItemId, e);
      return "error: " + e.getMessage();
    } catch (IllegalArgumentException e) {
      log.error("Invalid quantity: {}", quantity, e);
      return "error: " + e.getMessage();
    } catch (Exception e) {
      log.error("Error updating cart item: {}", cartItemId, e);
      return "error: 系统错误，请稍后重试";
    }
  }

  @PostMapping("/remove/{cartItemId}")
  @ResponseBody
  public String removeFromCart(@PathVariable("cartItemId") Long cartItemId,
      Authentication authentication,
      HttpSession session) {
    String sessionId = getSessionId(session);
    User user = authentication != null ? userService.findByEmail(authentication.getName()).orElse(null) : null;

    try {
      shoppingCartService.removeFromCart(cartItemId, user, sessionId);
      return "success";
    } catch (Exception e) {
      log.error("Error removing cart item: {}", cartItemId, e);
      return "error: " + e.getMessage();
    }
  }

  @PostMapping("/clear")
  @ResponseBody
  public String clearCart(Authentication authentication, HttpSession session) {
    String sessionId = getSessionId(session);
    User user = authentication != null ? userService.findByEmail(authentication.getName()).orElse(null) : null;

    try {
      shoppingCartService.clearCart(user, sessionId);
      return "success";
    } catch (Exception e) {
      log.error("Error clearing cart", e);
      return "error: " + e.getMessage();
    }
  }

  @GetMapping("/count")
  @ResponseBody
  public int getCartItemCount(Authentication authentication, HttpSession session) {
    String sessionId = getSessionId(session);
    User user = authentication != null ? userService.findByEmail(authentication.getName()).orElse(null) : null;
    return shoppingCartService.getCartItemCount(user, sessionId);
  }

  @GetMapping("/check")
  @ResponseBody
  public Map<String, Boolean> checkProductInCart(@RequestParam Long productId,
      @AuthenticationPrincipal UserDetails userDetails,
      HttpSession session) {
    User user = null;
    if (userDetails != null) {
      user = userService.findByEmail(userDetails.getUsername())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    String sessionId = session.getId();
    boolean inCart = shoppingCartService.isProductInCart(productId, user, sessionId);
    return Map.of("inCart", inCart);
  }

  private String getSessionId(HttpSession session) {
    String sessionId = (String) session.getAttribute(ANONYMOUS_CART_SESSION_KEY);
    if (sessionId == null) {
      sessionId = UUID.randomUUID().toString();
      session.setAttribute(ANONYMOUS_CART_SESSION_KEY, sessionId);
    }
    return sessionId;
  }
}
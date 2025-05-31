package com.example.eshop.controller;

import com.example.eshop.model.CartItem;
import com.example.eshop.model.User;
import com.example.eshop.model.UserCoupon;
import com.example.eshop.service.CartService;
import com.example.eshop.service.UserService;
import com.example.eshop.service.UserAddressService;
import com.example.eshop.service.OrderService;
import com.example.eshop.service.CouponService;
import com.example.eshop.dto.CouponResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

  private final CartService cartService;
  private final UserService userService;
  private final UserAddressService userAddressService;
  private final OrderService orderService;
  private final CouponService couponService;
  private final ObjectMapper objectMapper;

  @Autowired
  public CheckoutController(CartService cartService,
      UserService userService,
      UserAddressService userAddressService,
      OrderService orderService,
      CouponService couponService,
      ObjectMapper objectMapper) {
    this.cartService = cartService;
    this.userService = userService;
    this.userAddressService = userAddressService;
    this.orderService = orderService;
    this.couponService = couponService;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  public String checkout(Model model, @AuthenticationPrincipal User user) throws Exception {
    List<CartItem> cartItems = cartService.getCartItems(user);
    BigDecimal totalAmount = BigDecimal.valueOf(cartService.calculateTotalPrice(cartItems));

    // 获取用户所有优惠券，包括使用状态
    List<CouponResponse> userCoupons = couponService.getUserCouponsWithStatus(user.getId());

    model.addAttribute("cartItems", cartItems);
    model.addAttribute("totalAmount", totalAmount);
    model.addAttribute("addresses", userAddressService.getCurrentUserAddresses());
    model.addAttribute("walletBalance", userService.getWalletBalance(user.getEmail()));
    model.addAttribute("availablePoints", userService.getAvailablePoints(user.getEmail()));
    model.addAttribute("userCoupons", userCoupons);
    model.addAttribute("userCouponsJson", objectMapper.writeValueAsString(userCoupons));

    return "order/checkout";
  }
}
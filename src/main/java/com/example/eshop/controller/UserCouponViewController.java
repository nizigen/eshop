package com.example.eshop.controller;

import com.example.eshop.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user/coupons")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserCouponViewController {

  private final CouponService couponService;

  @GetMapping
  public String viewCoupons(@RequestAttribute Long userId, Model model) {
    model.addAttribute("availableCoupons", couponService.getActiveCoupons());
    model.addAttribute("myCoupons", couponService.getUserCoupons(userId));
    return "user/coupon/index";
  }
}
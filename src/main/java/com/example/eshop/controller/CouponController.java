package com.example.eshop.controller;

import com.example.eshop.dto.CouponRequest;
import com.example.eshop.dto.CouponResponse;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.service.CouponService;
import com.example.eshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

  private final CouponService couponService;
  private final UserService userService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(couponService.createCoupon(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CouponResponse> updateCoupon(
      @PathVariable Long id,
      @Valid @RequestBody CouponRequest request) {
    return ResponseEntity.ok(couponService.updateCoupon(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
    couponService.deleteCoupon(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CouponResponse> getCoupon(@PathVariable Long id) {
    return ResponseEntity.ok(couponService.getCoupon(id));
  }

  @GetMapping
  public ResponseEntity<List<CouponResponse>> getAllCoupons() {
    return ResponseEntity.ok(couponService.getAllCoupons());
  }

  @GetMapping("/active")
  public ResponseEntity<List<CouponResponse>> getActiveCoupons() {
    return ResponseEntity.ok(couponService.getActiveCoupons());
  }

  @PostMapping("/{id}/claim")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'USER')")
  public ResponseEntity<Map<String, Object>> claimCoupon(
      @PathVariable Long id,
      Authentication authentication) {
    Long userId = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        .getId();
    boolean claimed = couponService.claimCoupon(userId, id);
    Map<String, Object> result = new HashMap<>();
    result.put("success", claimed);
    result.put("message", claimed ? "领取成功" : "领取失败");
    return ResponseEntity.ok(result);
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<List<CouponResponse>> getMyCoupons(Authentication authentication) {
    Long userId = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        .getId();
    return ResponseEntity.ok(couponService.getUserCoupons(userId));
  }

  @GetMapping("/available")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<List<CouponResponse>> getAvailableCoupons(Authentication authentication) {
    Long userId = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        .getId();
    return ResponseEntity.ok(couponService.getAvailableCoupons(userId));
  }

  @GetMapping("/my/with-status")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<List<CouponResponse>> getMyCouponsWithStatus(Authentication authentication) {
    Long userId = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        .getId();
    return ResponseEntity.ok(couponService.getUserCouponsWithStatus(userId));
  }

  @GetMapping("/{id}/check-availability")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<Boolean> checkCouponAvailability(
      @PathVariable Long id,
      Authentication authentication) {
    Long userId = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        .getId();
    return ResponseEntity.ok(couponService.isCouponAvailableForUser(userId, id));
  }
}
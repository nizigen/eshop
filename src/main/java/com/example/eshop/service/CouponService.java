package com.example.eshop.service;

import com.example.eshop.dto.CouponRequest;
import com.example.eshop.dto.CouponResponse;
import com.example.eshop.model.Coupon;
import com.example.eshop.model.User;
import com.example.eshop.model.UserCoupon;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CouponService {
  // 查询用户可用的优惠券
  List<UserCoupon> findAvailableCoupons(User user, BigDecimal orderAmount);

  // 查询用户的所有优惠券
  List<UserCoupon> findUserCoupons(User user);

  // 用户领取优惠券
  UserCoupon receiveCoupon(User user, Long couponId);

  // 使用优惠券
  void useCoupon(UserCoupon userCoupon, Long orderId);

  // 验证优惠券是否可用
  boolean validateCoupon(UserCoupon userCoupon, BigDecimal orderAmount);

  // 计算优惠金额
  BigDecimal calculateDiscount(UserCoupon userCoupon, BigDecimal orderAmount);

  // 查找特定优惠券
  Optional<Coupon> findCouponById(Long couponId);

  // 查找用户优惠券
  Optional<UserCoupon> findUserCouponById(Long userCouponId);

  CouponResponse createCoupon(CouponRequest request);

  CouponResponse updateCoupon(Long id, CouponRequest request);

  void deleteCoupon(Long id);

  CouponResponse getCoupon(Long id);

  List<CouponResponse> getAllCoupons();

  List<CouponResponse> getActiveCoupons();

  boolean claimCoupon(Long userId, Long couponId);

  List<CouponResponse> getUserCoupons(Long userId);

  // 新增方法：获取可领取的优惠券列表
  List<CouponResponse> getAvailableCoupons(Long userId);

  // 新增方法：获取用户已领取的优惠券列表（包括使用状态）
  List<CouponResponse> getUserCouponsWithStatus(Long userId);

  // 新增方法：检查优惠券是否可用
  boolean isCouponAvailableForUser(Long userId, Long couponId);
}
package com.example.eshop.service.impl;

import com.example.eshop.dto.CouponRequest;
import com.example.eshop.dto.CouponResponse;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.*;
import com.example.eshop.repository.CouponRepository;
import com.example.eshop.repository.UserCouponRepository;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

  private final CouponRepository couponRepository;
  private final UserCouponRepository userCouponRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<UserCoupon> findAvailableCoupons(User user, BigDecimal orderAmount) {
    System.out.println("Finding available coupons for user: " + user.getId() + ", order amount: " + orderAmount);

    List<UserCoupon> userCoupons = userCouponRepository.findByUserAndStatus(user, CouponStatus.UNUSED);
    System.out.println("Found " + userCoupons.size() + " unused coupons for user");

    List<UserCoupon> availableCoupons = userCoupons.stream()
        .filter(uc -> {
          boolean isValid = uc.getCoupon().isValid();
          boolean isApplicable = uc.getCoupon().isApplicable(orderAmount);
          System.out
              .println("Coupon " + uc.getCoupon().getId() + " - Valid: " + isValid + ", Applicable: " + isApplicable);
          return isValid && isApplicable;
        })
        .toList();

    System.out.println("Returning " + availableCoupons.size() + " available coupons");
    return availableCoupons;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserCoupon> findUserCoupons(User user) {
    return userCouponRepository.findByUser(user);
  }

  @Override
  @Transactional
  public UserCoupon receiveCoupon(User user, Long couponId) {
    Coupon coupon = couponRepository.findById(couponId)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

    if (!coupon.isValid()) {
      throw new IllegalStateException("Coupon is not valid");
    }

    // 检查用户是否已经领取过这张优惠券
    if (userCouponRepository.existsByUserAndCoupon(user, coupon)) {
      throw new IllegalStateException("User already has this coupon");
    }

    // 创建用户优惠券记录
    UserCoupon userCoupon = new UserCoupon();
    userCoupon.setUser(user);
    userCoupon.setCoupon(coupon);
    userCoupon.setStatus(CouponStatus.UNUSED);
    userCoupon.setReceivedAt(LocalDateTime.now());

    // 更新优惠券剩余数量
    coupon.setRemaining(coupon.getRemaining() - 1);
    couponRepository.save(coupon);

    return userCouponRepository.save(userCoupon);
  }

  @Override
  @Transactional
  public void useCoupon(UserCoupon userCoupon, Long orderId) {
    if (!userCoupon.isUsable()) {
      throw new IllegalStateException("Coupon is not usable");
    }

    userCoupon.setStatus(CouponStatus.USED);
    userCoupon.setUsedAt(LocalDateTime.now());
    // 设置使用的订单ID
    Order order = new Order();
    order.setId(orderId);
    userCoupon.setUsedOrder(order);

    userCouponRepository.save(userCoupon);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean validateCoupon(UserCoupon userCoupon, BigDecimal orderAmount) {
    return userCoupon != null &&
        userCoupon.isUsable() &&
        userCoupon.getCoupon().isApplicable(orderAmount);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal calculateDiscount(UserCoupon userCoupon, BigDecimal orderAmount) {
    if (!validateCoupon(userCoupon, orderAmount)) {
      return BigDecimal.ZERO;
    }
    return userCoupon.getCoupon().calculateDiscount(orderAmount);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Coupon> findCouponById(Long couponId) {
    return couponRepository.findById(couponId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserCoupon> findUserCouponById(Long userCouponId) {
    return userCouponRepository.findById(userCouponId);
  }

  @Override
  public CouponResponse createCoupon(CouponRequest request) {
    Coupon coupon = new Coupon();
    updateCouponFromRequest(coupon, request);
    coupon.setRemaining(request.getQuantity());
    return CouponResponse.fromEntity(couponRepository.save(coupon));
  }

  @Override
  public CouponResponse updateCoupon(Long id, CouponRequest request) {
    Coupon coupon = couponRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
    updateCouponFromRequest(coupon, request);
    return CouponResponse.fromEntity(couponRepository.save(coupon));
  }

  @Override
  public void deleteCoupon(Long id) {
    if (!couponRepository.existsById(id)) {
      throw new ResourceNotFoundException("Coupon not found with id: " + id);
    }
    couponRepository.deleteById(id);
  }

  @Override
  public CouponResponse getCoupon(Long id) {
    return couponRepository.findById(id)
        .map(CouponResponse::fromEntity)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
  }

  @Override
  public List<CouponResponse> getAllCoupons() {
    return couponRepository.findAll().stream()
        .map(CouponResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public List<CouponResponse> getActiveCoupons() {
    return couponRepository.findAllActiveCoupons().stream()
        .map(CouponResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public boolean claimCoupon(Long userId, Long couponId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    Coupon coupon = couponRepository.findById(couponId)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));

    if (!coupon.isValid()) {
      return false;
    }

    // Check if user already has this coupon
    if (userCouponRepository.existsByUserAndCoupon(user, coupon)) {
      return false;
    }

    // Try to decrement the remaining count
    int updated = couponRepository.decrementRemainingCount(couponId);
    if (updated == 0) {
      return false;
    }

    // Create user coupon record
    UserCoupon userCoupon = new UserCoupon();
    userCoupon.setUser(user);
    userCoupon.setCoupon(coupon);
    userCoupon.setStatus(CouponStatus.UNUSED);
    userCoupon.setReceivedAt(LocalDateTime.now());
    userCouponRepository.save(userCoupon);

    return true;
  }

  @Override
  public List<CouponResponse> getUserCoupons(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    return userCouponRepository.findByUser(user).stream()
        .map(userCoupon -> CouponResponse.fromEntity(userCoupon.getCoupon()))
        .collect(Collectors.toList());
  }

  @Override
  public List<CouponResponse> getAvailableCoupons(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    // 获取所有有效的优惠券
    List<Coupon> activeCoupons = couponRepository.findAllActiveCoupons();

    // 获取用户已领取的优惠券ID列表
    List<Long> userCouponIds = userCouponRepository.findByUser(user).stream()
        .map(uc -> uc.getCoupon().getId())
        .collect(Collectors.toList());

    // 过滤掉用户已领取的优惠券
    return activeCoupons.stream()
        .filter(coupon -> !userCouponIds.contains(coupon.getId()) && coupon.getRemaining() > 0)
        .map(CouponResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public List<CouponResponse> getUserCouponsWithStatus(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    return userCouponRepository.findByUser(user).stream()
        .map(userCoupon -> {
          CouponResponse response = CouponResponse.fromEntity(userCoupon.getCoupon());
          response.setUsed(userCoupon.getStatus() == CouponStatus.USED);
          response.setUsedAt(userCoupon.getUsedAt());
          response.setActive(!userCoupon.getCoupon().isExpired() &&
              userCoupon.getCoupon().getRemaining() > 0 &&
              userCoupon.getStatus() == CouponStatus.UNUSED);
          return response;
        })
        .collect(Collectors.toList());
  }

  @Override
  public boolean isCouponAvailableForUser(Long userId, Long couponId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    Coupon coupon = couponRepository.findById(couponId)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));

    // 检查优惠券是否有效
    if (!coupon.isValid() || coupon.getRemaining() <= 0) {
      return false;
    }

    // 检查用户是否已经领取过这张优惠券
    return !userCouponRepository.existsByUserAndCoupon(user, coupon);
  }

  private void updateCouponFromRequest(Coupon coupon, CouponRequest request) {
    coupon.setName(request.getName());
    coupon.setType(request.getType());
    coupon.setValue(request.getValue());
    coupon.setMinPurchase(request.getMinPurchase());
    coupon.setStartTime(request.getStartTime());
    coupon.setEndTime(request.getEndTime());
    coupon.setQuantity(request.getQuantity());
  }
}
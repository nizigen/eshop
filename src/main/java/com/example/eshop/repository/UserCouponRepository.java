package com.example.eshop.repository;

import com.example.eshop.model.Coupon;
import com.example.eshop.model.User;
import com.example.eshop.model.UserCoupon;
import com.example.eshop.model.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
  List<UserCoupon> findByUser(User user);

  List<UserCoupon> findByUserAndStatus(User user, CouponStatus status);

  boolean existsByUserAndCoupon(User user, Coupon coupon);
}
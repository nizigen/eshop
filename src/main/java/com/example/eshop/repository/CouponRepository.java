package com.example.eshop.repository;

import com.example.eshop.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

  List<Coupon> findByEndTimeAfterAndRemainingGreaterThanOrderByCreatedAtDesc(LocalDateTime now, Integer minRemaining);

  @Query("SELECT CURRENT_TIMESTAMP")
  LocalDateTime getCurrentTimestamp();

  @Query("SELECT c FROM Coupon c WHERE c.startTime <= CURRENT_TIMESTAMP AND c.endTime >= CURRENT_TIMESTAMP AND c.remaining > 0")
  List<Coupon> findAllActiveCoupons();

  @Modifying
  @Query("UPDATE Coupon c SET c.remaining = c.remaining - 1 WHERE c.id = :couponId AND c.remaining > 0")
  int decrementRemainingCount(Long couponId);
}
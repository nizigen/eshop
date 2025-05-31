package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_coupons")
public class UserCoupon {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "coupon_id", nullable = false)
  private Coupon coupon;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CouponStatus status;

  @Column(name = "received_at")
  private LocalDateTime receivedAt;

  @Column(name = "used_at")
  private LocalDateTime usedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "used_order_id")
  private Order usedOrder;

  public boolean isUsable() {
    return status == CouponStatus.UNUSED &&
        coupon != null &&
        coupon.isValid();
  }
}
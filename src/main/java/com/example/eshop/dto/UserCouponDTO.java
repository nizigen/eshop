package com.example.eshop.dto;

import lombok.Data;

@Data
public class UserCouponDTO {
  private Long id; // user_coupon.id
  private String status; // user_coupon.status
  private Long couponId; // coupon.id
  private String name; // coupon.name
  private String type; // coupon.type
  private Double value; // coupon.value
  private Double minPurchase;// coupon.min_purchase
  private String startTime; // coupon.start_time
  private String endTime; // coupon.end_time
}
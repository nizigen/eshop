package com.example.eshop.dto;

import com.example.eshop.model.CouponType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponResponse {
  private Long id;
  private String name;
  private CouponType type;
  private BigDecimal value;
  private BigDecimal minPurchase;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer quantity;
  private Integer remaining;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean isExpired;
  private boolean isActive;
  private boolean hasStock;
  private boolean used;
  private LocalDateTime usedAt;

  public static CouponResponse fromEntity(com.example.eshop.model.Coupon coupon) {
    CouponResponse response = new CouponResponse();
    response.setId(coupon.getId());
    response.setName(coupon.getName());
    response.setType(coupon.getType());
    response.setValue(coupon.getValue());
    response.setMinPurchase(coupon.getMinPurchase());
    response.setStartTime(coupon.getStartTime());
    response.setEndTime(coupon.getEndTime());
    response.setQuantity(coupon.getQuantity());
    response.setRemaining(coupon.getRemaining());
    response.setCreatedAt(coupon.getCreatedAt());
    response.setUpdatedAt(coupon.getUpdatedAt());
    response.setExpired(coupon.isExpired());
    response.setActive(!coupon.isExpired() && coupon.getRemaining() > 0);
    response.setHasStock(coupon.hasStock());
    return response;
  }
}
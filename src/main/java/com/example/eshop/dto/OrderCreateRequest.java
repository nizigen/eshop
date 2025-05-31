package com.example.eshop.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class OrderCreateRequest {
  @NotNull(message = "Address ID is required")
  private Long addressId;

  @NotNull(message = "Shipping type is required")
  private String shippingType; // "express" or "offline"

  private String offlineTime;
  private String offlineLocation;

  @NotNull(message = "Points usage option is required")
  private boolean usePoints;

  @Min(value = 1, message = "Quantity must be at least 1")
  private int quantity;

  private Long productId; // For direct purchase from product detail page

  private Long userCouponId;
}
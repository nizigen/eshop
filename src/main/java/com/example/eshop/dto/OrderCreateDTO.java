package com.example.eshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderCreateDTO {
  private Long addressId;
  private String shippingType;
  private String offlineTime;
  private String offlineLocation;
  private Boolean usePoints;
  private Integer pointsToUse;
  private Double totalAmount;
  private Integer pointsDeducted;
  private Double deductedAmount;
}
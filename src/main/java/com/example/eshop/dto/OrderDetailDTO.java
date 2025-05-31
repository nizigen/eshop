package com.example.eshop.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailDTO {
  private Long id;
  private String orderNumber;
  private String status;
  private String sellerName;
  private String receiverName;
  private String receiverPhone;
  private String shippingProvince;
  private String shippingCity;
  private String shippingDistrict;
  private String detailedAddress;
  private BigDecimal totalAmount;
  private Integer pointsDeducted;
  private BigDecimal deductedAmount;
  private Long feeAmount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime shipTime;
  private String agreedOfflineTime;
  private String agreedOfflineLocation;
  private List<OrderItemDTO> items;
}
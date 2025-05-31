package com.example.eshop.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
  private Long id;
  private String orderNumber;
  private String status;
  private double totalAmount;
  private double deductedAmount;
  private int pointsDeducted;
  private String agreedOfflineTime;
  private String agreedOfflineLocation;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime paymentTime;
  private LocalDateTime deliveryTime;
  private LocalDateTime completionTime;
  private List<OrderItemDTO> items;
  private boolean isReviewed;
  private double couponDiscount;
}
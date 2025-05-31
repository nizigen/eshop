package com.example.eshop.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderListDTO {
  private Long id;
  private String orderNumber;
  private String status;
  private String sellerName;
  private LocalDateTime createdAt;
  private LocalDateTime shipTime;
  private BigDecimal totalAmount;
  private BigDecimal deductedAmount;
  private Integer pointsDeducted;
  private Long feeAmount;
  private List<OrderItemDTO> items;
}
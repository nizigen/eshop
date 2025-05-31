package com.example.eshop.dto;

import com.example.eshop.model.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OrderItemDTO {
  private Long id;
  private Long productId;
  private String productName;
  private String productImageUrl;
  private Integer quantity;
  private BigDecimal price;
  private Long sellerId;
  private String sellerName;
  private String status;
  private String returnReason;
}
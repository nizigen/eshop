package com.example.eshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CartItemDTO implements Serializable {
  private Long productId;
  private Integer quantity;
  private String productName;
  private Double price;
  private String imageUrl;
  private String productImage;
  private Long sellerId;
  private String sellerName;
}
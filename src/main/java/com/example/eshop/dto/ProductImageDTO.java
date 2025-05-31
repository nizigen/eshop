package com.example.eshop.dto;

import lombok.Data;

@Data
public class ProductImageDTO {
  private Long id;
  private String imageUrl;
  private boolean isPrimary;
}
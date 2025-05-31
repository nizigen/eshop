package com.example.eshop.dto;

import lombok.Data;

@Data
public class ProductSearchDto {
  private String keyword;
  private String sortBy = "default"; // default, price_asc, price_desc, sales, rating
  private int page = 0;
  private int size = 10;
}
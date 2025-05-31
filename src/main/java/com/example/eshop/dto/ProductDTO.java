package com.example.eshop.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
  private Long id;
  private String name;
  private double price;
  private List<ProductImageDTO> images;
}
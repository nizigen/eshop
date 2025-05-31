package com.example.eshop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewDto {

  // No need for IDs here, those come from path/context

  @NotNull(message = "Product rating cannot be null")
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  private Integer productRating;

  @Size(max = 1000, message = "Product comment cannot exceed 1000 characters")
  private String productComment;

  // Add fields for seller review if submitted in the same form
  @NotNull(message = "Seller service rating cannot be null")
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  private Integer sellerServiceRating;

  @Size(max = 1000, message = "Seller service comment cannot exceed 1000 characters")
  private String sellerServiceComment;
}
package com.example.eshop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
  @NotNull(message = "商品评分不能为空")
  @Min(value = 1, message = "商品评分最小为1")
  @Max(value = 5, message = "商品评分最大为5")
  private Integer productRating;

  @NotNull(message = "商品评价不能为空")
  private String productComment;

  @NotNull(message = "服务评分不能为空")
  @Min(value = 1, message = "服务评分最小为1")
  @Max(value = 5, message = "服务评分最大为5")
  private Integer serviceRating;

  @NotNull(message = "服务评价不能为空")
  private String serviceComment;
}
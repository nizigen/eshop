package com.example.eshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class SellerLevelUpdateDTO {
  @Min(1)
  @Max(5)
  private Integer level;

  public String getLevelDescription() {
    switch (level) {
      case 1:
        return "等级1 - 手续费0.1%";
      case 2:
        return "等级2 - 手续费0.2%";
      case 3:
        return "等级3 - 手续费0.5%";
      case 4:
        return "等级4 - 手续费0.75%";
      case 5:
        return "等级5 - 手续费1%";
      default:
        return "未知等级";
    }
  }
}
package com.example.eshop.dto;

import com.example.eshop.model.CouponType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
  @NotBlank(message = "优惠券名称不能为空")
  @Size(max = 100)
  private String name;

  @NotNull(message = "优惠券类型不能为空")
  private CouponType type;

  @NotNull(message = "优惠值不能为空")
  @DecimalMin(value = "0.01", message = "优惠值必须大于0")
  private BigDecimal value;

  @NotNull(message = "最低消费金额不能为空")
  @DecimalMin(value = "0.00")
  private BigDecimal minPurchase;

  @NotNull(message = "生效时间不能为空")
  @Future(message = "生效时间必须是未来时间")
  private LocalDateTime startTime;

  @NotNull(message = "失效时间不能为空")
  @Future(message = "失效时间必须是未来时间")
  private LocalDateTime endTime;

  @NotNull(message = "发放数量不能为空")
  @Min(value = 1, message = "发放数量必须大于0")
  private Integer quantity;
}
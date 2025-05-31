package com.example.eshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.RoundingMode;

@Data
@Entity
@Table(name = "coupons")
public class Coupon {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "优惠券名称不能为空")
  @Size(max = 100)
  @Column(nullable = false)
  private String name;

  @NotNull(message = "优惠券类型不能为空")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CouponType type;

  @NotNull(message = "优惠值不能为空")
  @DecimalMin(value = "0.01", message = "优惠值必须大于0")
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal value;

  @NotNull(message = "最低消费金额不能为空")
  @DecimalMin(value = "0.00")
  @Column(name = "min_purchase", nullable = false, precision = 10, scale = 2)
  private BigDecimal minPurchase;

  @NotNull(message = "生效时间不能为空")
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @NotNull(message = "失效时间不能为空")
  @Column(name = "end_time", nullable = false)
  private LocalDateTime endTime;

  @NotNull(message = "发放数量不能为空")
  @Min(value = 1, message = "发放数量必须大于0")
  @Column(nullable = false)
  private Integer quantity;

  @NotNull(message = "剩余数量不能为空")
  @Min(value = 0, message = "剩余数量不能小于0")
  @Column(nullable = false)
  private Integer remaining;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(endTime);
  }

  public boolean isNotStarted() {
    return LocalDateTime.now().isBefore(startTime);
  }

  public boolean isActive() {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(startTime) && now.isBefore(endTime);
  }

  public boolean isValid() {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(startTime) &&
        now.isBefore(endTime) &&
        remaining > 0;
  }

  public boolean hasStock() {
    return remaining > 0;
  }

  public boolean isApplicable(BigDecimal orderAmount) {
    return orderAmount.compareTo(minPurchase) >= 0;
  }

  public BigDecimal calculateDiscount(BigDecimal orderAmount) {
    if (type == CouponType.FIXED) {
      return value;
    } else {
      // 百分比折扣，计算折扣金额
      return orderAmount.multiply(value.divide(BigDecimal.valueOf(100)))
          .setScale(2, RoundingMode.HALF_UP);
    }
  }
}
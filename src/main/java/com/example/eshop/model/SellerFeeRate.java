package com.example.eshop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "seller_fee_rates")
@Data
public class SellerFeeRate {
  @Id
  @Column(name = "level")
  private Integer level;

  @Column(name = "fee_rate", precision = 5, scale = 4, nullable = false)
  private BigDecimal feeRate;

  @Column(name = "description")
  private String description;
}
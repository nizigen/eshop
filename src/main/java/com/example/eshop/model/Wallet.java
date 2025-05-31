package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallets")
public class Wallet {
  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
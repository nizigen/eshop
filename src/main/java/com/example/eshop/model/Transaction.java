package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionType type;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
  private BigDecimal balanceAfter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "related_order_id")
  private Order relatedOrder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "related_pending_fund_id")
  private PendingFund relatedPendingFund;

  private String description;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
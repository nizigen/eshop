package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_funds", indexes = {
    @Index(name = "idx_pending_seller_id", columnList = "seller_id"),
    @Index(name = "idx_pending_status_date", columnList = "status, release_due_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingFund implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Unique constraint ensures one pending fund record per order item
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_item_id", nullable = false, unique = true)
  private OrderItem orderItem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal feeRate;

  @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
  private BigDecimal platformFee;

  @Column(name = "release_due_date", nullable = false)
  private LocalDateTime releaseDueDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PendingFundStatus status = PendingFundStatus.HELD; // Default from schema

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
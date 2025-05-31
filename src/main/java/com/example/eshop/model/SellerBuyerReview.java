package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_buyer_reviews", uniqueConstraints = {
    @UniqueConstraint(name = "uk_seller_review_order", columnNames = { "order_id", "seller_id" })
}, indexes = {
    @Index(name = "idx_sb_review_seller_id", columnList = "seller_id"),
    @Index(name = "idx_sb_review_buyer_id", columnList = "buyer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerBuyerReview implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller; // Reviewer (Seller)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id", nullable = false)
  private User buyer; // User being reviewed (Buyer)

  @Column(name = "is_positive")
  private Boolean isPositive;

  @Lob
  private String comment;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
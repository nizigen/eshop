package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shopping_cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cart_user_product", columnNames = { "user_id", "product_id" })
}, indexes = {
    @Index(name = "idx_cart_user_id", columnList = "user_id"),
    @Index(name = "idx_cart_product_id", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartItem implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private Integer quantity = 1; // Default quantity

  @Column(name = "session_id")
  private String sessionId;

  @CreationTimestamp
  @Column(name = "added_at", updatable = false)
  private LocalDateTime addedAt;

  // Helper method to calculate subtotal
  public BigDecimal getSubtotal() {
    if (product == null) {
      return BigDecimal.ZERO;
    }
    return product.getPrice().multiply(BigDecimal.valueOf(quantity));
  }
}
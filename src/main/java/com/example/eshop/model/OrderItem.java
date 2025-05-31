package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "product_id"),
    @Index(name = "idx_order_item_seller_id", columnList = "seller_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  @JsonIgnore
  @JsonBackReference("order-items")
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  @JsonIgnore
  @JsonManagedReference("product-items")
  private Product product;

  // Redundant seller_id, consider removing if product.seller is sufficient
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  @JsonBackReference("seller-items")
  private Seller seller;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price; // Price snapshot at time of order

  @Column(name = "product_name", length = 255)
  private String productName; // Name snapshot at time of order

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderItemStatus status = OrderItemStatus.NORMAL; // Default from schema

  @OneToMany(mappedBy = "orderItem")
  @JsonIgnore
  private List<Review> reviews;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    OrderItem orderItem = (OrderItem) o;
    return id != null && id.equals(orderItem.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
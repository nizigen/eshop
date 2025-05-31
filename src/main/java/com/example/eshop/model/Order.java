package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", nullable = false, unique = true, length = 64)
  private String orderNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status = OrderStatus.PENDING_PAYMENT;

  @Column(name = "points_deducted")
  private Integer pointsDeducted = 0;

  @Column(name = "deducted_amount", precision = 10, scale = 2)
  private BigDecimal deductedAmount = BigDecimal.ZERO;

  @Column(name = "agreed_offline_time", length = 100)
  private String agreedOfflineTime;

  @Column(name = "agreed_offline_location", length = 255)
  private String agreedOfflineLocation;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "payment_time")
  private LocalDateTime paymentTime;

  @Column(name = "ship_time")
  private LocalDateTime shipTime;

  @Column(name = "delivery_time")
  private LocalDateTime deliveryTime;

  @Column(name = "completion_time")
  private LocalDateTime completionTime;

  @Column(name = "return_request_time")
  private LocalDateTime returnRequestTime;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JsonManagedReference("order-items")
  private List<OrderItem> orderItems = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  @JsonBackReference("seller-orders")
  private User seller;

  @Column(name = "fee_amount")
  private Long feeAmount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "address_id")
  @JsonBackReference("address-orders")
  private UserAddress address;

  public void addOrderItem(OrderItem item) {
    orderItems.add(item);
    item.setOrder(this);
  }

  public void removeOrderItem(OrderItem item) {
    orderItems.remove(item);
    item.setOrder(null);
  }

  public List<OrderItem> getItems() {
    return orderItems;
  }

  @PrePersist
  protected void onCreate() {
    if (this.orderNumber == null) {
      this.orderNumber = "ORD-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }
    createdAt = LocalDateTime.now();
  }
}
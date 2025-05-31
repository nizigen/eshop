package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "return_requests")
public class ReturnRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL)
  private List<ReturnRequestItem> returnRequestItems;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReturnReason reason;

  @Column(columnDefinition = "TEXT")
  private String description;

  @ElementCollection
  @CollectionTable(name = "return_request_images")
  @Column(name = "image_path")
  private List<String> images;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReturnStatus status = ReturnStatus.PENDING;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public enum ReturnReason {
    QUALITY_ISSUE,
    WRONG_ITEM,
    DAMAGED,
    NOT_AS_DESCRIBED,
    OTHER
  }

  public enum ReturnStatus {
    PENDING,
    APPROVED,
    REJECTED,
    COMPLETED
  }
}
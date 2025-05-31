package com.example.eshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.eshop.enums.SellerStatus;

@Entity
@Table(name = "seller_details")
@Data
@NoArgsConstructor
public class SellerDetails {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  @Size(max = 100)
  @Column(name = "store_name", nullable = false)
  private String storeName;

  @Size(max = 500)
  @Column(name = "store_description")
  private String storeDescription;

  @Size(max = 1024)
  @Column(name = "business_license_url", nullable = false)
  private String businessLicenseUrl;

  @Column(name = "seller_level", nullable = false)
  private Integer sellerLevel = 5;

  @Column(name = "seller_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private SellerStatus sellerStatus = SellerStatus.PENDING;

  @Column(name = "service_rating", precision = 3, scale = 2)
  private BigDecimal serviceRating = new BigDecimal("5.00");

  @Column(name = "buyer_positive_rating_percent", precision = 5, scale = 2)
  private BigDecimal buyerPositiveRatingPercent = new BigDecimal("100.00");

  @Column(name = "created_at", updatable = false)
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

  public SellerDetails(User user) {
    this.user = user;
    this.userId = user.getId();
    this.sellerLevel = 5;
    this.sellerStatus = SellerStatus.PENDING;
    this.serviceRating = new BigDecimal("5.00");
    this.buyerPositiveRatingPercent = new BigDecimal("100.00");
  }

  public Integer getSellerLevel() {
    return sellerLevel;
  }

  public void setSellerLevel(Integer sellerLevel) {
    this.sellerLevel = sellerLevel;
  }

  public SellerStatus getSellerStatus() {
    return sellerStatus;
  }

  public void setSellerStatus(SellerStatus sellerStatus) {
    this.sellerStatus = sellerStatus;
  }

  public BigDecimal getServiceRating() {
    return serviceRating;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SellerDetails that = (SellerDetails) o;
    return userId != null && userId.equals(that.userId);
  }

  @Override
  public int hashCode() {
    return userId != null ? userId.hashCode() : 0;
  }
}
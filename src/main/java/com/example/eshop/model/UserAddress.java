package com.example.eshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_addresses")
@Data
@NoArgsConstructor
public class UserAddress {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @Size(max = 50)
  @Column(name = "receiver_name", nullable = false)
  private String receiverName;

  @Size(max = 20)
  @Column(name = "receiver_phone", nullable = false)
  private String receiverPhone;

  @Size(max = 50)
  @Column(name = "shipping_province", nullable = false)
  private String shippingProvince;

  @Size(max = 50)
  @Column(name = "shipping_city", nullable = false)
  private String shippingCity;

  @Size(max = 50)
  @Column(name = "shipping_district", nullable = false)
  private String shippingDistrict;

  @Size(max = 200)
  @Column(name = "detailed_address", nullable = false)
  private String detailedAddress;

  @Version
  @Column(name = "version")
  private Long version;

  @Column(name = "is_default", nullable = false)
  private boolean defaultAddress = false;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (version == null) {
      version = 0L;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public String getFullAddress() {
    return String.format("%s %s %s %s", shippingProvince, shippingCity, shippingDistrict, detailedAddress);
  }

  public void setFullAddress(String fullAddress) {
    if (fullAddress == null || fullAddress.trim().isEmpty()) {
      return;
    }

    String[] parts = fullAddress.split(" ", 4);
    if (parts.length >= 4) {
      this.shippingProvince = parts[0];
      this.shippingCity = parts[1];
      this.shippingDistrict = parts[2];
      this.detailedAddress = parts[3];
    } else {
      this.detailedAddress = fullAddress;
      this.shippingProvince = "未知";
      this.shippingCity = "未知";
      this.shippingDistrict = "未知";
    }
  }

  public boolean isDefault() {
    return defaultAddress;
  }

  public void setDefault(boolean defaultAddress) {
    this.defaultAddress = defaultAddress;
  }
}
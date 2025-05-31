package com.example.eshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
public class UserProfile {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  @Size(max = 25)
  @Column(name = "bank_account_number")
  private String bankAccountNumber;

  @Size(max = 1024)
  @Column(name = "id_card_image_url")
  private String idCardImageUrl;

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

  public UserProfile(User user) {
    this.user = user;
    this.userId = user.getId();
  }

  // Compatibility methods for legacy code
  public String getBankAccount() {
    return this.bankAccountNumber;
  }

  public void setBankAccount(String bankAccount) {
    this.bankAccountNumber = bankAccount;
  }

  public String getStoreName() {
    if (user instanceof Seller) {
      return ((Seller) user).getStoreName();
    }
    return null;
  }

  public void setStoreName(String storeName) {
    if (user instanceof Seller) {
      ((Seller) user).setStoreName(storeName);
    }
  }
}
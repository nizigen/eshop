package com.example.eshop.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.example.eshop.enums.SellerStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.OneToMany;

@Entity
@DiscriminatorValue("ROLE_SELLER")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Seller extends User {

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private SellerDetails sellerDetails;

  @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<Product> products = new ArrayList<>();

  public Integer getLevel() {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    return sellerDetails.getSellerLevel();
  }

  public void setLevel(Integer level) {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    sellerDetails.setSellerLevel(level);
  }

  public SellerStatus getSellerStatus() {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    return sellerDetails.getSellerStatus();
  }

  public void setSellerStatus(SellerStatus sellerStatus) {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    sellerDetails.setSellerStatus(sellerStatus);
  }

  public String getStoreName() {
    return sellerDetails != null ? sellerDetails.getStoreName() : null;
  }

  public void setStoreName(String storeName) {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    sellerDetails.setStoreName(storeName);
  }

  public String getStoreDescription() {
    return sellerDetails != null ? sellerDetails.getStoreDescription() : null;
  }

  public void setStoreDescription(String storeDescription) {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    sellerDetails.setStoreDescription(storeDescription);
  }

  public String getBusinessLicenseUrl() {
    return sellerDetails != null ? sellerDetails.getBusinessLicenseUrl() : null;
  }

  public void setBusinessLicenseUrl(String businessLicenseUrl) {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    sellerDetails.setBusinessLicenseUrl(businessLicenseUrl);
  }

  public BigDecimal getServiceRating() {
    return sellerDetails != null ? sellerDetails.getServiceRating() : new BigDecimal("5.00");
  }

  public void setServiceRating(BigDecimal serviceRating) {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    sellerDetails.setServiceRating(serviceRating);
  }

  public BigDecimal getBuyerPositiveRatingPercent() {
    return sellerDetails != null ? sellerDetails.getBuyerPositiveRatingPercent() : new BigDecimal("100.00");
  }

  public void setBuyerPositiveRatingPercent(BigDecimal buyerPositiveRatingPercent) {
    if (sellerDetails == null) {
      sellerDetails = new SellerDetails();
      sellerDetails.setUser(this);
    }
    sellerDetails.setBuyerPositiveRatingPercent(buyerPositiveRatingPercent);
  }

  // Address is now handled through UserAddress
  public String getAddress() {
    return getAddresses().stream()
        .filter(addr -> addr.isDefault())
        .map(UserAddress::getFullAddress)
        .findFirst()
        .orElse(null);
  }

  public void setAddress(String address) {
    if (address == null || address.trim().isEmpty()) {
      return;
    }

    UserAddress defaultAddress = getAddresses().stream()
        .filter(addr -> addr.isDefault())
        .findFirst()
        .orElseGet(() -> {
          UserAddress newAddr = new UserAddress();
          newAddr.setUser(this);
          newAddr.setDefault(true);
          getAddresses().add(newAddr);
          return newAddr;
        });

    defaultAddress.setFullAddress(address);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Seller seller = (Seller) o;
    return getId() != null && getId().equals(seller.getId());
  }

  @Override
  public int hashCode() {
    return getId() != null ? getId().hashCode() : 0;
  }
}
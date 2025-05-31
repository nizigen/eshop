package com.example.eshop.dto;

import com.example.eshop.enums.SellerStatus;
import lombok.Data;

@Data
public class SellerDTO {
  private Long id;
  private String username;
  private String email;
  private String phone;
  private String storeName;
  private String storeDescription;
  private String businessLicenseUrl;
  private Integer level;
  private SellerStatus sellerStatus;
  private Double serviceRating;
  private Double buyerPositiveRatingPercent;
  private String city;
  private String gender;

  public void setSellerLevel(Integer sellerLevel) {
    this.level = sellerLevel;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }
}
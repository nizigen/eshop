package com.example.eshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressDTO {
  private Long id;

  @NotBlank(message = "Receiver name is required")
  @Size(max = 50, message = "Receiver name cannot exceed 50 characters")
  private String receiverName;

  @NotBlank(message = "Receiver phone is required")
  @Size(max = 20, message = "Receiver phone cannot exceed 20 characters")
  private String receiverPhone;

  @NotBlank(message = "Province is required")
  @Size(max = 50, message = "Province cannot exceed 50 characters")
  private String province;

  @NotBlank(message = "City is required")
  @Size(max = 50, message = "City cannot exceed 50 characters")
  private String city;

  @NotBlank(message = "District is required")
  @Size(max = 50, message = "District cannot exceed 50 characters")
  private String district;

  @NotBlank(message = "Detailed address is required")
  @Size(max = 200, message = "Detailed address cannot exceed 200 characters")
  private String detailedAddress;

  private boolean isDefault;

  // Helper method to maintain backward compatibility
  public String getFullAddress() {
    return String.format("%s %s %s %s", province, city, district, detailedAddress);
  }

  public void setFullAddress(String fullAddress) {
    if (fullAddress == null || fullAddress.trim().isEmpty()) {
      return;
    }

    String[] parts = fullAddress.split(" ", 4);
    if (parts.length >= 4) {
      this.province = parts[0];
      this.city = parts[1];
      this.district = parts[2];
      this.detailedAddress = parts[3];
    } else {
      this.detailedAddress = fullAddress;
      this.province = "未知";
      this.city = "未知";
      this.district = "未知";
    }
  }
}
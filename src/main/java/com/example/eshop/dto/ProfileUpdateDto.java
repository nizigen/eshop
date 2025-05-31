package com.example.eshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDto {
  @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
  private String name;

  @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
  private String phone;

  @Email(message = "Invalid email format")
  private String email;

  @Size(max = 100, message = "City name cannot exceed 100 characters")
  private String city;

  @Size(max = 10, message = "Gender cannot exceed 10 characters")
  private String gender;

  @Size(max = 200, message = "Address cannot exceed 200 characters")
  private String address;

  @Size(max = 50, message = "WeChat account cannot exceed 50 characters")
  private String wechat;

  @Size(max = 500, message = "Introduction cannot exceed 500 characters")
  private String bio;

  @Size(max = 25, message = "Bank account number cannot exceed 25 characters")
  private String bankAccountNumber;

  @Size(max = 1024, message = "ID card image URL cannot exceed 1024 characters")
  private String idCardImageUrl;
}
package com.example.eshop.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SellerRegistrationDto {

  @NotBlank(message = "Username is mandatory")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "Name is mandatory")
  @Size(min = 2, max = 50)
  private String name;

  @NotBlank(message = "Password is mandatory")
  @Size(min = 6, message = "Password must be at least 6 characters long")
  private String password;

  @NotBlank(message = "Password confirmation cannot be blank")
  private String confirmPassword;

  @NotBlank(message = "Phone number is mandatory")
  @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
  private String phone;

  @Email(message = "Email should be valid")
  @NotBlank(message = "Email is mandatory")
  private String email;

  @Size(max = 100)
  private String city;

  @Size(max = 10)
  private String gender;

  @NotBlank(message = "Bank account number is mandatory")
  @Pattern(regexp = "^[0-9]{16}$", message = "Bank account number must be 16 digits")
  private String bankAccountNumber;

  @NotBlank(message = "Verification code is mandatory")
  @Size(min = 4, max = 4, message = "Verification code must be 4 characters")
  private String captcha;

  @NotBlank(message = "Store name is mandatory")
  @Size(min = 2, max = 100)
  private String storeName;

  @Size(max = 500)
  private String storeDescription;

  // Fields for file uploads will be handled in the Controller/Service
  // We might add validation annotations like @NotNull here if the DTO receives
  // the MultipartFile directly
  // private MultipartFile businessLicenseFile;
  // private MultipartFile idCardFile;

}
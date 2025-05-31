package com.example.eshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

  @NotBlank(message = "Username is required")
  private String username;

  @NotBlank(message = "Verification code is required")
  private String verificationCode;

  @NotBlank(message = "New password is required")
  private String newPassword;
}
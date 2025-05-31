package com.example.eshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeDto {
  @NotBlank(message = "Current password is required")
  private String currentPassword;

  @NotBlank(message = "New password is required")
  @Size(min = 6, message = "Password must be at least 6 characters long")
  private String newPassword;

  @NotBlank(message = "Password confirmation is required")
  private String confirmPassword;
}
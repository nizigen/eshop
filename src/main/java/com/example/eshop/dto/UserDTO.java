package com.example.eshop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
  private Long id;
  private String username;
  private String email;
  private String phone;
  private String name;
  private String avatarUrl;
  private String role;
  private double walletBalance;
  private int points;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean enabled;
  private String wechat;
  private String bankAccountNumber;
  private String idCardImageUrl;
}
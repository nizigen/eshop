package com.example.eshop.dto;

import com.example.eshop.enums.Gender;
import com.example.eshop.enums.UserRole;
import com.example.eshop.enums.UserStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
  private Long id;
  private String username;
  private String phone;
  private String email;
  private String city;
  private Gender gender;
  private UserRole role;
  private UserStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String name;
  private String avatarUrl;
  private String bio;
  private String wechat;
}
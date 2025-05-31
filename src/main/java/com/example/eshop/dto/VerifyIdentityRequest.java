package com.example.eshop.dto;

import lombok.Data;

@Data
public class VerifyIdentityRequest {
  private String username;
  private String captcha;
}
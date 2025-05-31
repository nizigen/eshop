package com.example.eshop.dto;

import lombok.Data;

@Data
public class CaptchaResponse {
  private String captchaId;
  private String captchaImage;
}
package com.example.eshop.service;

import com.example.eshop.dto.CaptchaResponse;

public interface CaptchaService {
  CaptchaResponse generateCaptcha();

  boolean validateCaptcha(String captchaId, String captchaCode);
}
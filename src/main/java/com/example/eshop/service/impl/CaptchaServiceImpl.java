package com.example.eshop.service.impl;

import com.example.eshop.dto.CaptchaResponse;
import com.example.eshop.service.CaptchaService;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {
  private final ConcurrentHashMap<String, String> captchaStore = new ConcurrentHashMap<>();
  private final Random random = new Random();

  @Override
  public CaptchaResponse generateCaptcha() {
    String captchaId = UUID.randomUUID().toString();
    String captchaCode = generateRandomCode();
    captchaStore.put(captchaId, captchaCode);

    CaptchaResponse response = new CaptchaResponse();
    response.setCaptchaId(captchaId);
    response.setCaptchaImage(generateCaptchaImage(captchaCode));
    return response;
  }

  @Override
  public boolean validateCaptcha(String captchaId, String captchaCode) {
    String storedCode = captchaStore.get(captchaId);
    if (storedCode != null && storedCode.equals(captchaCode)) {
      captchaStore.remove(captchaId);
      return true;
    }
    return false;
  }

  private String generateRandomCode() {
    return String.format("%06d", random.nextInt(1000000));
  }

  private String generateCaptchaImage(String code) {
    return code;
  }
}
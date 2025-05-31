package com.example.eshop.exception;

// Simple RuntimeException for CAPTCHA validation failures.
public class InvalidCaptchaException extends RuntimeException {
  public InvalidCaptchaException(String message) {
    super(message);
  }

  public InvalidCaptchaException(String message, Throwable cause) {
    super(message, cause);
  }
}
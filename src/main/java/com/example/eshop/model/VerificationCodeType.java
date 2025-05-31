package com.example.eshop.model;

public enum VerificationCodeType {
  REGISTER("注册验证"),
  LOGIN("登录验证"),
  RESET_PASSWORD("重置密码");

  private final String description;

  VerificationCodeType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
package com.example.eshop.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private int code;

  private ApiResponse(boolean success, String message, T data, int code) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.code = code;
  }

  public static <T> ApiResponse<T> success(String message) {
    return success(message, null);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data, 200);
  }

  public static <T> ApiResponse<T> error(String message) {
    return error(message, null);
  }

  public static <T> ApiResponse<T> error(String message, T data) {
    return new ApiResponse<>(false, message, data, 400);
  }

  public static <T> ApiResponse<T> error(String message, int code) {
    return new ApiResponse<>(false, message, null, code);
  }

  public static <T> ApiResponse<T> error(String message, T data, int code) {
    return new ApiResponse<>(false, message, data, code);
  }
}
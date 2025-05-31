package com.example.eshop.exception;

public class InsufficientPointsException extends RuntimeException {
  public InsufficientPointsException(String message) {
    super(message);
  }
}
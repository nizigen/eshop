package com.example.eshop.exception;

// Extending AuthenticationException might be suitable if it happens during an authentication flow context,
// otherwise, a custom RuntimeException is also fine.
public class UserAlreadyExistAuthenticationException extends RuntimeException { // Or AuthenticationException
  public UserAlreadyExistAuthenticationException(String message) {
    super(message);
  }

  public UserAlreadyExistAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
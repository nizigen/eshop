package com.example.eshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN) // Return 403 Forbidden
public class UnauthorizedActionException extends RuntimeException {

  public UnauthorizedActionException(String message) {
    super(message);
  }

  public UnauthorizedActionException(String message, Throwable cause) {
    super(message, cause);
  }
}
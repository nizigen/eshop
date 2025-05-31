package com.example.eshop.exception;

import com.example.eshop.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiResponse<Void> handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.error("Resource not found: {}", ex.getMessage());
    return ApiResponse.error(ex.getMessage(), 404);
  }

  @ExceptionHandler(UnauthorizedActionException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiResponse<Void> handleUnauthorizedActionException(UnauthorizedActionException ex) {
    log.error("Unauthorized action: {}", ex.getMessage());
    return ApiResponse.error(ex.getMessage(), 403);
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException ex) {
    log.error("Access denied: {}", ex.getMessage());
    return ApiResponse.error("Access denied", 403);
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponse<Void> handleBadCredentialsException(BadCredentialsException ex) {
    log.error("Bad credentials: {}", ex.getMessage());
    return ApiResponse.error("Invalid username or password", 401);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    log.error("Validation error: {}", errors);
    return ResponseEntity.badRequest()
        .body(ApiResponse.<Map<String, String>>error("Validation failed", errors));
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleBindExceptions(BindException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    log.error("Binding error: {}", errors);
    return ResponseEntity.badRequest()
        .body(ApiResponse.<Map<String, String>>error("Invalid request parameters", errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<String> handleConstraintViolationException(ConstraintViolationException ex) {
    String message = ex.getConstraintViolations().stream()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .collect(Collectors.joining(", "));
    log.error("Constraint violation: {}", message);
    return ApiResponse.error(message, 400);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
    log.error("Data integrity violation: {}", ex.getMessage());
    return ApiResponse.error("Data integrity violation", 409);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
    log.error("File size exceeded: {}", ex.getMessage());
    return ApiResponse.error("File size exceeded the maximum allowed size", 400);
  }

  @ExceptionHandler(InvalidCaptchaException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleInvalidCaptchaException(InvalidCaptchaException ex) {
    log.error("Invalid captcha: {}", ex.getMessage());
    return ApiResponse.error("Invalid verification code", 400);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Void> handleAllUncaughtException(Exception ex) {
    log.error("Uncaught error occurred", ex);
    return ApiResponse.error("An unexpected error occurred", 500);
  }
}
package com.example.eshop.service;

import com.example.eshop.dto.CustomerRegistrationDto;
import com.example.eshop.dto.SellerRegistrationDto;
import com.example.eshop.model.UserProfile;
import com.example.eshop.model.SellerDetails;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

  /**
   * Registers a new customer.
   * Checks for existing users and validates CAPTCHA before saving.
   * Throws exceptions on validation failure or if user already exists.
   * 
   * @param registrationDto DTO containing customer registration data.
   * @param profile         User profile data
   */
  void registerCustomer(CustomerRegistrationDto registrationDto, UserProfile profile);

  /**
   * Registers a new seller.
   * Checks for existing users, validates CAPTCHA, and handles file uploads before
   * saving.
   * Throws exceptions on validation failure, file processing errors, or if user
   * already exists.
   * 
   * @param registrationDto DTO containing seller registration data.
   * @param sellerDetails   Seller details data
   * @param profile         User profile data
   */
  void registerSeller(SellerRegistrationDto registrationDto, SellerDetails sellerDetails, UserProfile profile);

  // Note: CAPTCHA generation is handled in AuthController now.
  // The validation logic remains implicitly within the register methods based on
  // the controller's check.
  // We could add a separate validateCaptcha method here if we want the service to
  // re-validate,
  // but the current flow validates in the controller before calling the service.

}
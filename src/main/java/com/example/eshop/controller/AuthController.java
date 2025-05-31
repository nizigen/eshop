package com.example.eshop.controller;

import com.example.eshop.dto.CustomerRegistrationDto;
import com.example.eshop.dto.SellerRegistrationDto;
import com.example.eshop.exception.InvalidCaptchaException;
import com.example.eshop.exception.UserAlreadyExistAuthenticationException;
import com.example.eshop.model.UserProfile;
import com.example.eshop.model.SellerDetails;
import com.example.eshop.model.User;
import com.example.eshop.service.AuthService;
import com.example.eshop.service.FileStorageService;
import com.example.eshop.dto.ApiResponse;
import com.example.eshop.dto.VerifyIdentityRequest;
import com.example.eshop.dto.ResetPasswordRequest;
import com.example.eshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/auth")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;
  private final CaptchaController captchaController;
  private final FileStorageService fileStorageService;
  private final UserService userService;

  @Autowired
  public AuthController(AuthService authService, CaptchaController captchaController,
      FileStorageService fileStorageService, UserService userService) {
    this.authService = authService;
    this.captchaController = captchaController;
    this.fileStorageService = fileStorageService;
    this.userService = userService;
  }

  // == Registration Form Display ==

  @GetMapping("/register")
  public String showRegistrationChoicePage() {
    log.info("Displaying registration choice page.");
    return "auth/register";
  }

  @GetMapping("/register-customer")
  public String showCustomerRegistrationForm(Model model) {
    log.info("Displaying customer registration form.");
    if (!model.containsAttribute("customerDto")) {
      model.addAttribute("customerDto", new CustomerRegistrationDto());
    }
    return "auth/register-customer";
  }

  @GetMapping("/register-seller")
  public String showSellerRegistrationForm(Model model) {
    log.info("Displaying seller registration form.");
    if (!model.containsAttribute("sellerDto")) {
      model.addAttribute("sellerDto", new SellerRegistrationDto());
    }
    return "auth/register-seller";
  }

  // == Registration Form Submission ==

  @PostMapping("/register-customer")
  public String processCustomerRegistration(@Valid @ModelAttribute("customerDto") CustomerRegistrationDto customerDto,
      BindingResult result, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "auth/register-customer";
    }

    try {
      // 检查用户名是否已存在
      if (userService.existsByUsername(customerDto.getUsername())) {
        result.rejectValue("username", "error.username", "用户名已被使用");
        return "auth/register-customer";
      }

      // 检查手机号是否已存在
      if (userService.existsByPhone(customerDto.getPhone())) {
        result.rejectValue("phone", "error.phone", "手机号已被注册");
        return "auth/register-customer";
      }

      // 检查邮箱是否已存在
      if (userService.existsByEmail(customerDto.getEmail())) {
        result.rejectValue("email", "error.email", "邮箱已被注册");
        return "auth/register-customer";
      }

      // 处理身份证照片上传
      String idCardImageUrl = null;
      if (customerDto.getIdCardFile() != null && !customerDto.getIdCardFile().isEmpty()) {
        idCardImageUrl = fileStorageService.storeFile(customerDto.getIdCardFile());
      }

      // 创建用户
      User user = userService.createCustomer(customerDto);

      // 创建用户档案
      UserProfile userProfile = new UserProfile();
      userProfile.setUser(user);
      userProfile.setBankAccountNumber(customerDto.getBankAccountNumber());
      userProfile.setIdCardImageUrl(idCardImageUrl);
      userService.save(userProfile);

      redirectAttributes.addFlashAttribute("success", "注册成功！请登录");
      return "redirect:/auth/login";
    } catch (Exception e) {
      log.error("Customer registration failed", e);
      redirectAttributes.addFlashAttribute("error", "注册失败：" + e.getMessage());
      return "redirect:/auth/register-customer";
    }
  }

  @PostMapping("/register-seller")
  public String processSellerRegistration(@Valid @ModelAttribute("sellerDto") SellerRegistrationDto sellerDto,
      BindingResult bindingResult,
      @RequestParam("businessLicenseFile") MultipartFile businessLicenseFile,
      @RequestParam("idCardFile") MultipartFile idCardFile,
      RedirectAttributes redirectAttributes,
      Model model,
      HttpSession session) {
    log.info("Processing seller registration for email: {}", sellerDto.getEmail());

    if (!validateRegistrationCommon(sellerDto, bindingResult, session)) {
      log.warn("Validation failed for seller registration");
      return "auth/register-seller";
    }

    if (businessLicenseFile.isEmpty() || idCardFile.isEmpty()) {
      model.addAttribute("errorMessage", "Please upload both business license and ID card files");
      return "auth/register-seller";
    }

    try {
      // Handle file uploads
      log.debug("Storing business license file...");
      String businessLicenseUrl = fileStorageService.storeFile(businessLicenseFile);
      log.debug("Business license file stored at: {}", businessLicenseUrl);

      log.debug("Storing ID card file...");
      String idCardImageUrl = fileStorageService.storeFile(idCardFile);
      log.debug("ID card file stored at: {}", idCardImageUrl);

      // Create seller details
      SellerDetails sellerDetails = new SellerDetails();
      sellerDetails.setStoreName(sellerDto.getStoreName());
      sellerDetails.setStoreDescription(sellerDto.getStoreDescription());
      sellerDetails.setBusinessLicenseUrl(businessLicenseUrl);

      // Create user profile
      UserProfile profile = new UserProfile();
      profile.setBankAccountNumber(sellerDto.getBankAccountNumber());
      profile.setIdCardImageUrl(idCardImageUrl);

      log.debug("Calling authService.registerSeller...");
      authService.registerSeller(sellerDto, sellerDetails, profile);

      log.info("Seller registration successful for email: {}", sellerDto.getEmail());
      redirectAttributes.addFlashAttribute("successMessage",
          "Seller registration successful! Your account is pending approval. You can log in once approved.");
      return "redirect:/auth/login";
    } catch (UserAlreadyExistAuthenticationException e) {
      log.warn("Registration failed: {}", e.getMessage());
      bindingResult.rejectValue("email", "error.sellerDto", e.getMessage());
      return "auth/register-seller";
    } catch (Exception e) {
      log.error("Error during seller registration: {}", e.getMessage(), e);
      model.addAttribute("errorMessage", "An unexpected error occurred during registration: " + e.getMessage());
      return "auth/register-seller";
    }
  }

  private boolean validateRegistrationCommon(Object dto, BindingResult bindingResult, HttpSession session) {
    if (bindingResult.hasErrors()) {
      log.warn("Form validation errors: {}", bindingResult.getAllErrors());
      return false;
    }

    if (dto instanceof CustomerRegistrationDto) {
      CustomerRegistrationDto customerDto = (CustomerRegistrationDto) dto;
      if (!customerDto.getPassword().equals(customerDto.getConfirmPassword())) {
        bindingResult.rejectValue("confirmPassword", "error.customerDto", "Passwords do not match");
        return false;
      }
      if (!captchaController.validateCaptcha(customerDto.getCaptcha(), session)) {
        bindingResult.rejectValue("captcha", "error.customerDto", "Invalid verification code");
        return false;
      }
    } else if (dto instanceof SellerRegistrationDto) {
      SellerRegistrationDto sellerDto = (SellerRegistrationDto) dto;
      if (!sellerDto.getPassword().equals(sellerDto.getConfirmPassword())) {
        bindingResult.rejectValue("confirmPassword", "error.sellerDto", "Passwords do not match");
        return false;
      }
      if (!captchaController.validateCaptcha(sellerDto.getCaptcha(), session)) {
        bindingResult.rejectValue("captcha", "error.sellerDto", "Invalid verification code");
        return false;
      }
    }
    return true;
  }

  // == Login Page ==
  @GetMapping("/login")
  public String showLoginPage(@RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "logout", required = false) String logout,
      Model model) {
    if (error != null) {
      model.addAttribute("errorMessage", "Invalid username or password.");
    }
    if (logout != null) {
      model.addAttribute("logoutMessage", "You have been logged out successfully.");
    }
    log.info("Displaying login page.");
    return "auth/login";
  }

  // == Forgot Password ==
  @GetMapping("/forgot-password")
  public String showForgotPasswordPage() {
    return "auth/forgot-password";
  }

  @PostMapping("/verify-identity")
  @ResponseBody
  public ResponseEntity<ApiResponse<String>> verifyIdentity(@RequestBody @Valid VerifyIdentityRequest request) {
    try {
      // Verify user exists and email matches
      if (!userService.existsByUsername(request.getUsername())) {
        return ResponseEntity.ok(ApiResponse.error("User not found"));
      }

      return ResponseEntity.ok(ApiResponse.success("Verification code sent successfully"));
    } catch (Exception e) {
      log.error("Error during identity verification: {}", e.getMessage(), e);
      return ResponseEntity.ok(ApiResponse.error("Error during identity verification"));
    }
  }

  @PostMapping("/reset-password")
  @ResponseBody
  public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
    try {
      userService.resetPassword(request.getUsername(), request.getNewPassword());
      return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    } catch (Exception e) {
      log.error("Error during password reset: {}", e.getMessage(), e);
      return ResponseEntity.ok(ApiResponse.error("Error during password reset"));
    }
  }
}
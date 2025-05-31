package com.example.eshop.service.impl;

import com.example.eshop.dto.CustomerRegistrationDto;
import com.example.eshop.dto.SellerRegistrationDto;
import com.example.eshop.exception.UserAlreadyExistAuthenticationException;
import com.example.eshop.model.*;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.repository.UserProfileRepository;
import com.example.eshop.repository.SellerDetailsRepository;
import com.example.eshop.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final SellerDetailsRepository sellerDetailsRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public AuthServiceImpl(
      UserRepository userRepository,
      UserProfileRepository userProfileRepository,
      SellerDetailsRepository sellerDetailsRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
    this.sellerDetailsRepository = sellerDetailsRepository;
    this.passwordEncoder = passwordEncoder;
  }

  private void checkUserExists(String email, String phone) {
    log.debug("Checking existence for phone: {}", phone);
    if (userRepository.existsByPhone(phone)) {
      log.warn("Phone number already exists: {}", phone);
      throw new UserAlreadyExistAuthenticationException("Phone number already exists: " + phone);
    }
    if (email != null) {
      log.debug("Checking existence for email: {}", email);
      if (userRepository.existsByEmail(email)) {
        log.warn("Email already exists: {}", email);
        throw new UserAlreadyExistAuthenticationException("Email already exists: " + email);
      }
    }
    log.debug("User existence check passed for phone: {}, email: {}", phone, email);
  }

  @Override
  public void registerCustomer(CustomerRegistrationDto registrationDto, UserProfile profile) {
    log.info("Registering new customer with email: {}", registrationDto.getEmail());
    checkUserExists(registrationDto.getEmail(), registrationDto.getPhone());

    // Create new customer
    Customer customer = new Customer();
    customer.setUsername(registrationDto.getUsername());
    customer.setName(registrationDto.getName());
    customer.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
    customer.setPhone(registrationDto.getPhone());
    customer.setEmail(registrationDto.getEmail());
    customer.setCity(registrationDto.getCity());
    customer.setGender(Gender.valueOf(registrationDto.getGender().toUpperCase()));
    customer.setStatus(UserStatus.ACTIVE);

    // Save customer
    Customer savedCustomer = userRepository.save(customer);
    log.debug("Customer saved with ID: {}", savedCustomer.getId());

    // Set up profile
    profile.setUser(savedCustomer);
    userProfileRepository.save(profile);
    log.info("Customer registration completed successfully for email: {}", registrationDto.getEmail());
  }

  @Override
  public void registerSeller(SellerRegistrationDto registrationDto, SellerDetails sellerDetails, UserProfile profile) {
    log.info("Registering new seller with email: {}", registrationDto.getEmail());
    checkUserExists(registrationDto.getEmail(), registrationDto.getPhone());

    // Create new seller
    Seller seller = new Seller();
    seller.setUsername(registrationDto.getUsername());
    seller.setName(registrationDto.getName());
    seller.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
    seller.setPhone(registrationDto.getPhone());
    seller.setEmail(registrationDto.getEmail());
    seller.setCity(registrationDto.getCity());
    seller.setGender(Gender.valueOf(registrationDto.getGender().toUpperCase()));
    seller.setStatus(UserStatus.PENDING);

    // Save seller
    Seller savedSeller = userRepository.save(seller);
    log.debug("Seller saved with ID: {}", savedSeller.getId());

    // Set up seller details
    sellerDetails.setUser(savedSeller);
    sellerDetailsRepository.save(sellerDetails);
    log.debug("Seller details saved for seller ID: {}", savedSeller.getId());

    // Set up profile
    profile.setUser(savedSeller);
    userProfileRepository.save(profile);
    log.info("Seller registration completed successfully for email: {}", registrationDto.getEmail());
  }
}
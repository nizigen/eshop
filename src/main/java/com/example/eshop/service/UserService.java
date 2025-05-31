package com.example.eshop.service;

import com.example.eshop.dto.ProfileUpdateDto;
import com.example.eshop.dto.UserDTO;
import com.example.eshop.dto.CustomerRegistrationDto;
import com.example.eshop.model.User;
import com.example.eshop.model.UserProfile;
import com.example.eshop.model.UserStatus;
import com.example.eshop.model.Seller;
import com.example.eshop.model.UserCoupon;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<Seller> findSellerByEmail(String email);

  Optional<User> findByPhone(String phone);

  Optional<UserProfile> findUserProfile(Long userId);

  UserProfile updateUserProfile(Long userId, ProfileUpdateDto profileDto);

  void deleteUserProfile(Long userId);

  List<User> findAllUsers();

  List<User> findUsersByStatus(UserStatus status);

  void approveUser(Long userId);

  void rejectUser(Long userId);

  Optional<User> findUserById(Long userId);

  void updateUser(User user);

  void deleteUser(Long userId);

  void updateBankAccount(Long userId, String bankAccountNumber);

  String updateIdCardImage(Long userId, MultipartFile file);

  void changePassword(Long userId, String currentPassword, String newPassword);

  String updateAvatar(Long userId, MultipartFile file);

  void updateWechat(Long userId, String wechat);

  boolean existsByUsername(String username);

  void resetPassword(String email, String newPassword);

  User getCurrentUser();

  UserDTO getCurrentUserDTO(String email);

  double getWalletBalance(String email);

  int getAvailablePoints(String email);

  void deductPoints(String email, int points);

  void addPoints(String email, int points);

  void updateWalletBalance(String email, double amount);

  UserDTO getUserProfile(Long userId);

  Double getWalletBalance(Long userId);

  Integer getPointsBalance(Long userId);

  long countAllUsers();

  long countUsersByStatus(UserStatus status);

  List<UserCoupon> getUserCoupons(String email);

  String uploadIdCardImage(MultipartFile file);

  void saveUserProfile(UserProfile profile);

  boolean existsByPhone(String phone);

  boolean existsByEmail(String email);

  User createCustomer(CustomerRegistrationDto dto);

  UserProfile save(UserProfile userProfile);
}
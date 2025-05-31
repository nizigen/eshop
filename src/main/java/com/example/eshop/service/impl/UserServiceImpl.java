package com.example.eshop.service.impl;

import com.example.eshop.dto.ProfileUpdateDto;
import com.example.eshop.dto.UserDTO;
import com.example.eshop.dto.CustomerRegistrationDto;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.Gender;
import com.example.eshop.model.User;
import com.example.eshop.model.UserProfile;
import com.example.eshop.model.UserStatus;
import com.example.eshop.model.Seller;
import com.example.eshop.model.UserCoupon;
import com.example.eshop.model.Role;
import com.example.eshop.model.Customer;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.repository.UserProfileRepository;
import com.example.eshop.repository.SellerRepository;
import com.example.eshop.service.UserService;
import com.example.eshop.service.WalletService;
import com.example.eshop.service.PointsService;
import com.example.eshop.service.FileStorageService;
import com.example.eshop.service.CouponService;
import com.example.eshop.config.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserProfileRepository userProfileRepository;

  @Autowired
  private SellerRepository sellerRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private FileStorageService fileStorageService;

  @Autowired
  private FileStorageProperties fileStorageProperties;

  @Autowired
  private WalletService walletService;

  @Autowired
  private PointsService pointsService;

  @Autowired
  private CouponService couponService;

  @Override
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public Optional<Seller> findSellerByEmail(String email) {
    return sellerRepository.findByEmail(email);
  }

  @Override
  public Optional<User> findByPhone(String phone) {
    return userRepository.findByPhone(phone);
  }

  @Override
  public Optional<UserProfile> findUserProfile(Long userId) {
    return userProfileRepository.findById(userId);
  }

  @Override
  public UserProfile updateUserProfile(Long userId, ProfileUpdateDto profileDto) {
    Optional<UserProfile> userProfileOptional = userProfileRepository.findById(userId);
    if (userProfileOptional.isPresent()) {
      UserProfile userProfile = userProfileOptional.get();
      if (profileDto.getBankAccountNumber() != null) {
        userProfile.setBankAccountNumber(profileDto.getBankAccountNumber());
      }
      if (profileDto.getIdCardImageUrl() != null) {
        userProfile.setIdCardImageUrl(profileDto.getIdCardImageUrl());
      }
      return userProfileRepository.save(userProfile);
    }
    throw new RuntimeException("User profile not found");
  }

  @Override
  public void deleteUserProfile(Long userId) {
    userProfileRepository.deleteById(userId);
  }

  @Override
  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  @Override
  public List<User> findUsersByStatus(UserStatus status) {
    return userRepository.findByStatus(status);
  }

  @Override
  public void approveUser(Long userId) {
    Optional<User> userOptional = userRepository.findById(userId);
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      user.setStatus(UserStatus.ACTIVE);
      userRepository.save(user);
    }
  }

  @Override
  @Transactional
  public void rejectUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setStatus(UserStatus.REJECTED);
    userRepository.save(user);
  }

  @Override
  public Optional<User> findUserById(Long userId) {
    return userRepository.findById(userId);
  }

  @Override
  @Transactional
  public void updateUser(User user) {
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    userRepository.deleteById(userId);
  }

  @Override
  @Transactional
  public void updateBankAccount(Long userId, String bankAccountNumber) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    UserProfile profile = userProfileRepository.findByUserId(userId)
        .orElseGet(() -> new UserProfile(user));
    profile.setBankAccountNumber(bankAccountNumber);
    userProfileRepository.save(profile);
  }

  @Override
  @Transactional
  public String updateIdCardImage(Long userId, MultipartFile file) {
    try {
      // 验证文件
      if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("No file uploaded");
      }

      // 验证文件类型
      String contentType = file.getContentType();
      if (contentType == null || !isAllowedFileType(contentType)) {
        throw new IllegalArgumentException("File type not allowed. Allowed types: JPG, PNG, PDF");
      }

      // 验证文件大小（5MB）
      if (file.getSize() > 5 * 1024 * 1024) {
        throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
      }

      // 存储文件到 idcards 目录
      String relativePath = fileStorageService.store(file, "idcards");
      String idCardImageUrl = relativePath; // 直接使用相对路径，不需要添加 /upload/ 前缀
      logger.info("Successfully stored ID card image at: {}", idCardImageUrl);

      // 获取用户
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      // 获取或创建用户档案
      UserProfile profile = userProfileRepository.findByUserId(userId)
          .orElseGet(() -> {
            UserProfile newProfile = new UserProfile(user);
            return newProfile;
          });

      // 更新身份证图片URL
      profile.setIdCardImageUrl(idCardImageUrl);
      userProfileRepository.save(profile);
      logger.info("Successfully updated user profile with ID card image URL: {}", idCardImageUrl);

      return idCardImageUrl;
    } catch (IOException e) {
      logger.error("Failed to store ID card image for user {}: {}", userId, e.getMessage(), e);
      throw new RuntimeException("Failed to store ID card file", e);
    }
  }

  private boolean isAllowedFileType(String contentType) {
    return contentType != null && (contentType.equals("image/jpeg") ||
        contentType.equals("image/png") ||
        contentType.equals("application/pdf"));
  }

  @Override
  @Transactional
  public void changePassword(Long userId, String currentPassword, String newPassword) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Override
  @Transactional
  public String updateAvatar(Long userId, MultipartFile file) {
    try {
      String avatarUrl = fileStorageService.storeFile(file);
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      user.setAvatarUrl(avatarUrl);
      userRepository.save(user);
      return avatarUrl;
    } catch (IOException e) {
      throw new RuntimeException("Failed to store avatar file", e);
    }
  }

  @Override
  @Transactional
  public void updateWechat(Long userId, String wechat) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setWechat(wechat);
    userRepository.save(user);
  }

  @Override
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  @Override
  @Transactional
  public void resetPassword(String email, String newPassword) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Override
  public User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  @Override
  public UserDTO getCurrentUserDTO(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return convertToDTO(user);
  }

  @Override
  public double getWalletBalance(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return walletService.getBalance(user);
  }

  @Override
  public int getAvailablePoints(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return pointsService.getAvailablePoints(user);
  }

  @Override
  public void deductPoints(String email, int points) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    pointsService.deductPoints(user, points);
  }

  @Override
  public void addPoints(String email, int points) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    pointsService.addPoints(user, points);
  }

  @Override
  public void updateWalletBalance(String email, double amount) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    walletService.updateBalance(user, amount);
  }

  @Override
  public UserDTO getUserProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return convertToDTO(user);
  }

  @Override
  public Double getWalletBalance(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return walletService.getBalance(user);
  }

  @Override
  public Integer getPointsBalance(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return pointsService.getPoints(userId);
  }

  @Override
  public List<UserCoupon> getUserCoupons(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return couponService.findUserCoupons(user);
  }

  private UserDTO convertToDTO(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setName(user.getName());
    dto.setEmail(user.getEmail());
    dto.setPhone(user.getPhone());
    dto.setAvatarUrl(user.getAvatarUrl());
    dto.setRole(user.getClass().getSimpleName().toUpperCase());
    dto.setWalletBalance(walletService.getBalance(user));
    dto.setPoints(pointsService.getAvailablePoints(user));
    dto.setCreatedAt(user.getCreatedAt());
    dto.setUpdatedAt(user.getUpdatedAt());
    dto.setEnabled(user.getStatus() == UserStatus.ACTIVE);
    dto.setWechat(user.getWechat());

    UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
    if (profile != null) {
      dto.setBankAccountNumber(profile.getBankAccountNumber());
      dto.setIdCardImageUrl(profile.getIdCardImageUrl());
    }

    return dto;
  }

  @Override
  public long countAllUsers() {
    return userRepository.count();
  }

  @Override
  public long countUsersByStatus(UserStatus status) {
    return userRepository.countByStatus(status);
  }

  @Override
  public String uploadIdCardImage(MultipartFile file) {
    try {
      // 验证文件
      if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("No file uploaded");
      }

      // 验证文件类型
      String contentType = file.getContentType();
      if (contentType == null || !isAllowedFileType(contentType)) {
        throw new IllegalArgumentException("File type not allowed. Allowed types: JPG, PNG, PDF");
      }

      // 验证文件大小（5MB）
      if (file.getSize() > 5 * 1024 * 1024) {
        throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
      }

      // 获取当前用户
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      User currentUser = findByEmail(auth.getName())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      // 存储文件到 idcards 目录
      String idCardImageUrl = fileStorageService.storeFile(file);
      logger.info("Successfully stored ID card image at: {}", idCardImageUrl);

      // 获取或创建用户档案
      UserProfile profile = userProfileRepository.findByUserId(currentUser.getId())
          .orElseGet(() -> {
            UserProfile newProfile = new UserProfile(currentUser);
            return newProfile;
          });

      // 更新身份证图片URL
      profile.setIdCardImageUrl(idCardImageUrl);
      userProfileRepository.save(profile);
      logger.info("Successfully updated user profile with ID card image URL: {}", idCardImageUrl);

      return idCardImageUrl;
    } catch (IOException e) {
      logger.error("Failed to store ID card image", e);
      throw new RuntimeException("Failed to store ID card file", e);
    }
  }

  @Override
  @Transactional
  public void saveUserProfile(UserProfile profile) {
    if (profile.getUser() == null) {
      throw new IllegalArgumentException("User profile must be associated with a user");
    }

    // 确保用户存在
    User user = userRepository.findById(profile.getUser().getId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // 设置关联关系
    profile.setUser(user);
    profile.setUserId(user.getId());

    // 保存档案
    UserProfile savedProfile = userProfileRepository.save(profile);

    // 验证保存是否成功
    if (savedProfile == null) {
      throw new RuntimeException("Failed to save user profile");
    }
  }

  @Override
  public boolean existsByPhone(String phone) {
    return userRepository.existsByPhone(phone);
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  public User createCustomer(CustomerRegistrationDto dto) {
    Customer customer = new Customer(
        dto.getUsername(),
        dto.getName(),
        passwordEncoder.encode(dto.getPassword()),
        dto.getPhone(),
        dto.getEmail(),
        dto.getCity(),
        Gender.valueOf(dto.getGender().toUpperCase()));
    return userRepository.save(customer);
  }

  @Override
  public UserProfile save(UserProfile userProfile) {
    return userProfileRepository.save(userProfile);
  }
}
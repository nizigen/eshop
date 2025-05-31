package com.example.eshop.controller;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.User;
import com.example.eshop.model.UserAddress;
import com.example.eshop.model.UserProfile;
import com.example.eshop.model.Gender;
import com.example.eshop.service.UserAddressService;
import com.example.eshop.service.UserService;
import com.example.eshop.dto.ProfileUpdateDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {
  private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

  private final UserService userService;
  private final UserAddressService userAddressService;

  public ProfileController(UserService userService, UserAddressService userAddressService) {
    this.userService = userService;
    this.userAddressService = userAddressService;
  }

  @GetMapping
  public String showProfile(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.findByEmail(auth.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    List<UserAddress> addresses = userAddressService.getCurrentUserAddresses();

    model.addAttribute("user", user);
    model.addAttribute("addresses", addresses);
    return "user/profile";
  }

  @PostMapping("/update")
  @ResponseBody
  public Map<String, Object> updateBasicProfile(@RequestBody ProfileUpdateDto profileDto) {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      User user = userService.findByEmail(auth.getName())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      // 验证必填字段
      if (profileDto.getPhone() != null && profileDto.getPhone().isEmpty()) {
        return Map.of(
            "success", false,
            "message", "手机号不能为空");
      }

      // 只更新基本信息
      if (profileDto.getName() != null && !profileDto.getName().isEmpty()) {
        user.setName(profileDto.getName());
      }
      if (profileDto.getPhone() != null) {
        user.setPhone(profileDto.getPhone());
      }
      if (profileDto.getEmail() != null && !profileDto.getEmail().isEmpty()) {
        user.setEmail(profileDto.getEmail());
      }
      if (profileDto.getCity() != null) {
        user.setCity(profileDto.getCity());
      }
      if (profileDto.getGender() != null) {
        user.setGender(Gender.valueOf(profileDto.getGender().toUpperCase()));
      }
      if (profileDto.getWechat() != null) {
        user.setWechat(profileDto.getWechat());
      }
      if (profileDto.getBio() != null) {
        user.setBio(profileDto.getBio());
      }

      userService.updateUser(user);
      return Map.of(
          "success", true,
          "message", "基本信息更新成功");
    } catch (ResourceNotFoundException e) {
      logger.error("User not found while updating profile: ", e);
      return Map.of(
          "success", false,
          "message", "用户不存在");
    } catch (Exception e) {
      logger.error("Error updating profile: ", e);
      return Map.of(
          "success", false,
          "message", "更新失败：" + e.getMessage());
    }
  }

  @PostMapping("/update-avatar")
  @ResponseBody
  public Map<String, Object> updateAvatar(@RequestParam("avatar") MultipartFile file) {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      User user = userService.findByEmail(auth.getName())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      String avatarUrl = userService.updateAvatar(user.getId(), file);
      return Map.of("success", true, "message", "头像更新成功", "avatarUrl", avatarUrl);
    } catch (Exception e) {
      logger.error("Error updating avatar: ", e);
      return Map.of("success", false, "message", "头像更新失败：" + e.getMessage());
    }
  }

  @PostMapping("/update-idcard")
  @ResponseBody
  public Map<String, Object> updateIdCard(@RequestParam("idCardImage") MultipartFile file) {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      User user = userService.findByEmail(auth.getName())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      String idCardImageUrl = userService.updateIdCardImage(user.getId(), file);
      logger.info("Successfully updated ID card image for user: {}", user.getId());
      return Map.of(
          "success", true,
          "message", "身份证图片更新成功",
          "idCardImageUrl", idCardImageUrl);
    } catch (Exception e) {
      logger.error("Error updating ID card image: ", e);
      return Map.of(
          "success", false,
          "message", "身份证图片更新失败：" + e.getMessage());
    }
  }

  @GetMapping("/additional")
  public String showAdditionalProfile(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.findByEmail(auth.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    UserProfile profile = userService.findUserProfile(user.getId())
        .orElseGet(() -> new UserProfile(user));

    model.addAttribute("user", user);
    model.addAttribute("profile", profile);
    return "user/additional-profile";
  }

  @PostMapping("/update-additional")
  @ResponseBody
  public Map<String, Object> updateAdditionalProfile(
      @RequestParam(required = false) String bankAccountNumber,
      @RequestParam(required = false) MultipartFile idCardImage,
      @RequestParam(required = false) String idCardImageUrl) {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      User user = userService.findByEmail(auth.getName())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      // 获取或创建用户档案
      UserProfile profile = userService.findUserProfile(user.getId())
          .orElseGet(() -> {
            UserProfile newProfile = new UserProfile(user);
            return newProfile;
          });

      boolean hasChanges = false;
      String message = "";
      String newImageUrl = null;

      // 更新银行卡号
      if (bankAccountNumber != null && !bankAccountNumber.equals(profile.getBankAccountNumber())) {
        profile.setBankAccountNumber(bankAccountNumber);
        hasChanges = true;
        message += "银行卡号已更新。";
      }

      // 处理身份证图片
      if (idCardImage != null && !idCardImage.isEmpty()) {
        try {
          // 验证文件类型
          String contentType = idCardImage.getContentType();
          if (contentType == null || !isAllowedFileType(contentType)) {
            return Map.of(
                "success", false,
                "message", "文件类型不支持。仅支持 JPG、PNG、PDF 格式");
          }

          // 验证文件大小（5MB）
          if (idCardImage.getSize() > 5 * 1024 * 1024) {
            return Map.of(
                "success", false,
                "message", "文件大小超过限制。最大支持 5MB");
          }

          // 使用 updateIdCardImage 方法上传图片
          newImageUrl = userService.updateIdCardImage(user.getId(), idCardImage);
          if (newImageUrl != null) {
            profile.setIdCardImageUrl(newImageUrl);
            hasChanges = true;
            message += "身份证图片已更新。";
            logger.info("Successfully uploaded ID card image for user: {}, URL: {}", user.getId(), newImageUrl);
          }
        } catch (Exception e) {
          logger.error("Failed to upload ID card image for user: {}", user.getId(), e);
          return Map.of(
              "success", false,
              "message", "身份证图片上传失败：" + e.getMessage());
        }
      }

      // 如果没有文件上传且银行卡号没有变化，返回提示
      if (!hasChanges) {
        return Map.of(
            "success", true,
            "message", "没有需要更新的内容");
      }

      // 保存更新后的档案
      userService.saveUserProfile(profile);
      logger.info("Successfully updated additional profile for user: {}", user.getId());

      // 构建响应
      Map<String, Object> response = new java.util.HashMap<>();
      response.put("success", true);
      response.put("message", message.trim());
      if (newImageUrl != null) {
        response.put("idCardImageUrl", newImageUrl);
      }
      return response;
    } catch (Exception e) {
      logger.error("Failed to update additional profile", e);
      return Map.of(
          "success", false,
          "message", "附加信息更新失败：" + e.getMessage());
    }
  }

  private boolean isAllowedFileType(String contentType) {
    return contentType != null && (contentType.equals("image/jpeg") ||
        contentType.equals("image/png") ||
        contentType.equals("application/pdf"));
  }
}
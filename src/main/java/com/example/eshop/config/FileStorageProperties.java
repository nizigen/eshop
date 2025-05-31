package com.example.eshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration properties for file storage.
 * This class manages all file storage related settings including upload
 * directories,
 * allowed file types, and size limits.
 */
@Component
@ConfigurationProperties(prefix = "app.file-storage")
@Validated
@Data
public class FileStorageProperties {

  @NotBlank(message = "Upload directory must not be empty")
  private String uploadDir = "E:/eshop/uploads";

  @NotBlank(message = "Avatar directory must not be empty")
  private String avatarDir = "avatars";

  @NotBlank(message = "Product directory must not be empty")
  private String productDir = "products";

  @NotBlank(message = "ID card directory must not be empty")
  private String idcardDir = "idcards";

  @NotBlank(message = "Allowed types must not be empty")
  private String allowedTypes = "image/jpeg,image/png,image/gif";

  @Min(value = 1024, message = "Max file size must be at least 1KB")
  private long maxFileSize = 5242880; // 5MB default

  /**
   * Returns a list of allowed MIME types.
   * 
   * @return List of allowed MIME types
   */
  public List<String> getAllowedTypesList() {
    return Arrays.asList(allowedTypes.split(","));
  }

  /**
   * Returns the full path for avatar storage.
   * 
   * @return Path object representing the avatar storage location
   */
  public Path getAvatarPath() {
    return Paths.get(uploadDir, avatarDir).normalize();
  }

  /**
   * Returns the full path for product image storage.
   * 
   * @return Path object representing the product image storage location
   */
  public Path getProductPath() {
    return Paths.get(uploadDir, productDir).normalize();
  }

  /**
   * Returns the full path for ID card storage.
   * 
   * @return Path object representing the ID card storage location
   */
  public Path getIdcardPath() {
    return Paths.get(uploadDir, idcardDir).normalize();
  }

  /**
   * Checks if a given MIME type is allowed.
   * 
   * @param mimeType The MIME type to check
   * @return true if the MIME type is allowed, false otherwise
   */
  public boolean isAllowedType(String mimeType) {
    return getAllowedTypesList().contains(mimeType.toLowerCase());
  }

  /**
   * Returns a formatted string of allowed file types for display.
   * 
   * @return A human-readable string of allowed file types
   */
  public String getFormattedAllowedTypes() {
    return allowedTypes.replace("image/", "").toUpperCase();
  }

  /**
   * Returns the maximum file size in megabytes.
   * 
   * @return The maximum file size in MB
   */
  public double getMaxFileSizeInMB() {
    return maxFileSize / (1024.0 * 1024.0);
  }
}
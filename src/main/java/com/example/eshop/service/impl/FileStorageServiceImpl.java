package com.example.eshop.service.impl;

import com.example.eshop.config.FileStorageProperties;
import com.example.eshop.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

  private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);
  private final Path fileStorageLocation;
  private final FileStorageProperties fileStorageProperties;

  @Autowired
  public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
    this.fileStorageProperties = fileStorageProperties;
    this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
        .toAbsolutePath().normalize();

    try {
      Files.createDirectories(this.fileStorageLocation);
      log.info("File storage location initialized at: {}", this.fileStorageLocation);
    } catch (IOException ex) {
      throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
    }
  }

  @Override
  public void init() throws IOException {
    try {
      Files.createDirectories(fileStorageLocation);
      Files.createDirectories(fileStorageProperties.getAvatarPath());
      Files.createDirectories(fileStorageProperties.getProductPath());
      Files.createDirectories(fileStorageLocation.resolve("idcards"));
      log.info("File storage initialized. Root: {}, Avatar dir: {}, Product dir: {}, ID cards dir: {}",
          fileStorageLocation,
          fileStorageProperties.getAvatarPath(),
          fileStorageProperties.getProductPath(),
          fileStorageLocation.resolve("idcards"));
    } catch (IOException e) {
      log.error("Could not initialize storage locations", e);
      throw e;
    }
  }

  @Override
  public String store(MultipartFile file, String subPath) throws IOException {
    validateFile(file);

    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
    validateFileName(fileName);

    // Validate file type
    String contentType = file.getContentType();
    if (contentType == null || !fileStorageProperties.isAllowedType(contentType)) {
      throw new RuntimeException("File type not allowed. Allowed types: " +
          fileStorageProperties.getFormattedAllowedTypes());
    }

    // Validate file size
    if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
      throw new RuntimeException(String.format("File size exceeds maximum limit of %.2f MB",
          fileStorageProperties.getMaxFileSizeInMB()));
    }

    String uniqueFileName = generateUniqueFileName(fileName);
    Path targetDirectory = this.fileStorageLocation.resolve(subPath).normalize();

    // 确保目标目录存在
    if (!Files.exists(targetDirectory)) {
      Files.createDirectories(targetDirectory);
      log.info("Created directory: {}", targetDirectory);
    }

    Path targetPath = targetDirectory.resolve(uniqueFileName);
    validateFilePath(targetPath);

    try (InputStream inputStream = file.getInputStream()) {
      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
      log.info("File stored successfully at: {}", targetPath);
    }

    String relativePath = subPath + "/" + uniqueFileName;
    log.debug("File stored successfully. Relative path: {}", relativePath);
    return relativePath;
  }

  @Override
  public Resource loadAsResource(String relativePath) throws IOException {
    try {
      Path filePath = fileStorageLocation.resolve(relativePath).normalize();
      validateFilePath(filePath);

      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists() && resource.isReadable()) {
        log.debug("File loaded successfully: {}", relativePath);
        return resource;
      } else {
        log.warn("File not found or not readable: {}", relativePath);
        throw new RuntimeException("Could not read file: " + relativePath);
      }
    } catch (MalformedURLException e) {
      log.error("Error loading file: {}", relativePath, e);
      throw new RuntimeException("Could not read file: " + relativePath, e);
    }
  }

  @Override
  public void delete(String relativePath) throws IOException {
    Path filePath = fileStorageLocation.resolve(relativePath).normalize();
    validateFilePath(filePath);

    if (Files.deleteIfExists(filePath)) {
      log.debug("File deleted successfully: {}", relativePath);
    } else {
      log.warn("File not found for deletion: {}", relativePath);
    }
  }

  @Override
  public void deleteDirectory(String relativePath) throws IOException {
    Path dirPath = fileStorageLocation.resolve(relativePath).normalize();
    validateDirectoryPath(dirPath);

    if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
      try (Stream<Path> walk = Files.walk(dirPath)) {
        walk.sorted(Comparator.reverseOrder())
            .forEach(path -> {
              try {
                Files.delete(path);
                log.debug("Deleted path: {}", path);
              } catch (IOException e) {
                log.error("Failed to delete path: {}", path, e);
                throw new RuntimeException("Failed to delete path: " + path, e);
              }
            });
      }
      log.info("Directory deleted successfully: {}", relativePath);
    }
  }

  @Override
  public Path getRootLocation() {
    return this.fileStorageLocation;
  }

  @Override
  public String storeFile(MultipartFile file) throws IOException {
    return store(file, "");
  }

  @Override
  public boolean deleteFile(String fileUrl) {
    try {
      delete(fileUrl);
      return true;
    } catch (IOException ex) {
      log.error("Error deleting file: {}", fileUrl, ex);
      return false;
    }
  }

  @Override
  public String getFilePath(String fileUrl) {
    Path path = this.fileStorageLocation.resolve(fileUrl).normalize();
    validateFilePath(path);
    return path.toString();
  }

  // Private helper methods
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Failed to store empty file.");
    }
  }

  private void validateFileName(String fileName) {
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      throw new RuntimeException("Filename contains invalid path sequence: " + fileName);
    }
  }

  private void validateFilePath(Path path) {
    if (!path.startsWith(this.fileStorageLocation)) {
      throw new RuntimeException("Cannot access file outside storage directory: " + path);
    }
  }

  private void validateDirectoryPath(Path path) {
    if (!path.startsWith(fileStorageLocation) || path.equals(fileStorageLocation)) {
      throw new RuntimeException("Cannot access directory outside or equal to storage root: " + path);
    }
  }

  private String generateUniqueFileName(String originalFileName) {
    String fileExtension = "";
    int lastDotIndex = originalFileName.lastIndexOf('.');
    if (lastDotIndex > 0) {
      fileExtension = originalFileName.substring(lastDotIndex);
    }
    return UUID.randomUUID().toString() + fileExtension;
  }

  private Path storeFileSecurely(MultipartFile file, Path targetDirectory, String fileName) throws IOException {
    Files.createDirectories(targetDirectory);
    Path targetPath = targetDirectory.resolve(fileName);
    validateFilePath(targetPath);

    try (InputStream inputStream = file.getInputStream()) {
      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
      return targetPath;
    }
  }
}
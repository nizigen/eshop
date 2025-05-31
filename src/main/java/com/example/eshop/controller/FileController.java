package com.example.eshop.controller;

import com.example.eshop.config.FileStorageProperties;
import com.example.eshop.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {

  private static final Logger log = LoggerFactory.getLogger(FileController.class);
  private final FileStorageService fileStorageService;
  private final FileStorageProperties fileStorageProperties;

  @Autowired
  public FileController(FileStorageService fileStorageService, FileStorageProperties fileStorageProperties) {
    this.fileStorageService = fileStorageService;
    this.fileStorageProperties = fileStorageProperties;
  }

  @GetMapping({ "/uploads/products/{productId}/{filename:.+}",
      "/products/{productId}/{filename:.+}",
      "/images/products/{productId}/{filename:.+}" })
  @ResponseBody
  public ResponseEntity<Resource> serveProductImage(@PathVariable String productId, @PathVariable String filename,
      HttpServletRequest request) {
    String relativePath = Paths.get("products", productId, filename).toString();
    log.debug("Serving product image from relative path: {}", relativePath);
    return serveFile(relativePath, request);
  }

  @GetMapping({ "/uploads/avatars/{userId}/{filename:.+}",
      "/avatars/{userId}/{filename:.+}" })
  @ResponseBody
  public ResponseEntity<Resource> serveAvatar(@PathVariable String userId, @PathVariable String filename,
      HttpServletRequest request) {
    String relativePath = Paths.get("avatars", userId, filename).toString();
    log.debug("Serving avatar from relative path: {}", relativePath);
    return serveFile(relativePath, request);
  }

  @GetMapping("/uploads/{filename:.+}")
  @ResponseBody
  public ResponseEntity<Resource> serveRootFile(@PathVariable String filename,
      HttpServletRequest request) {
    log.debug("Serving file from root uploads directory: {}", filename);
    return serveFile(filename, request);
  }

  @GetMapping({ "/uploads/idcards/{userId}/{filename:.+}",
      "/idcards/{userId}/{filename:.+}",
      "/images/idcards/{userId}/{filename:.+}" })
  @ResponseBody
  public ResponseEntity<Resource> serveIdCardImage(@PathVariable String userId, @PathVariable String filename,
      HttpServletRequest request) {
    String relativePath = Paths.get("idcards", userId, filename).toString();
    log.debug("Serving ID card image from relative path: {}", relativePath);
    return serveFile(relativePath, request);
  }

  private ResponseEntity<Resource> serveFile(String relativePath, HttpServletRequest request) {
    try {
      Resource resource = fileStorageService.loadAsResource(relativePath);

      // Try to determine file's content type
      String contentType = null;
      try {
        contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
      } catch (IOException ex) {
        log.debug("Could not determine file type.");
      }

      // Fallback to a safe type if detection failed
      if (contentType == null) {
        contentType = "application/octet-stream";
      }

      // Enable browser caching for images
      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000") // Cache for 1 year
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
          .body(resource);

    } catch (IOException e) {
      log.error("Error serving file {}: {}", relativePath, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
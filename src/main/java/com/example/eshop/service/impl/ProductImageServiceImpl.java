package com.example.eshop.service.impl;

import com.example.eshop.exception.ResourceNotFoundException; // For image not found
import com.example.eshop.exception.StorageException; // Import StorageException
import com.example.eshop.exception.UnauthorizedActionException; // For image not belonging to product
import com.example.eshop.model.Product;
import com.example.eshop.model.ProductImage;
import com.example.eshop.repository.ProductImageRepository;
import com.example.eshop.service.FileStorageService; // Import FileStorageService
import com.example.eshop.service.ProductImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductImageServiceImpl implements ProductImageService {

  private static final Logger log = LoggerFactory.getLogger(ProductImageServiceImpl.class);

  private final ProductImageRepository productImageRepository;
  private final FileStorageService fileStorageService; // Inject FileStorageService

  @Autowired
  public ProductImageServiceImpl(ProductImageRepository productImageRepository,
      FileStorageService fileStorageService) { // Inject FileStorageService
    this.productImageRepository = productImageRepository;
    this.fileStorageService = fileStorageService;
  }

  @Override
  @Transactional // Add transactionality
  public void saveImagesForProduct(Product product, List<MultipartFile> files) throws IOException {
    log.info("Saving images for product ID: {}", product.getId());
    if (product == null || product.getId() == null) {
      log.error("Cannot save images for a null product or product with null ID.");
      throw new IllegalArgumentException("Product and Product ID must not be null.");
    }

    // Define the sub-path for this product's images
    String productSubPath = "products/" + product.getId();

    boolean isFirstImage = product.getImages() == null || product.getImages().isEmpty();
    int successfulUploads = 0;

    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        log.warn("Skipping empty file during upload for product ID: {}", product.getId());
        continue;
      }
      try {
        // Use FileStorageService to store the file
        // The service returns the relative path including the generated filename
        String relativePath = fileStorageService.store(file, productSubPath);

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImagePath(relativePath); // Store the relative path returned by the service
        productImage.setIsPrimary(isFirstImage); // Set first valid uploaded image as primary

        productImageRepository.save(productImage);
        log.debug("Saved ProductImage entity for path: {}", relativePath);
        isFirstImage = false; // Only the first successfully processed image is set as primary
        successfulUploads++;
      } catch (StorageException | IOException e) {
        log.error("Failed to store image for product {}: {}", product.getId(), e.getMessage(), e);
        // Decide on error handling: continue with others or rethrow?
        // Rethrowing ensures transaction rollback if any image fails.
        throw new IOException("Failed to store one or more images for product " + product.getId(), e);
      }
    }
    log.info("Finished saving {} images for product ID: {}", successfulUploads, product.getId());
  }

  @Override
  @Transactional // Add transactionality
  public void deleteImagesForProduct(Product product) throws IOException {
    log.warn("Deleting all images for product ID: {}", product.getId());
    List<ProductImage> images = productImageRepository.findByProduct(product);

    for (ProductImage image : images) {
      deleteSingleProductImage(image);
    }

    // After deleting files and entities, delete the product's image directory
    if (product != null && product.getId() != null) {
      String productSubPath = "products/" + product.getId();
      try {
        fileStorageService.deleteDirectory(productSubPath);
        log.info("Attempted deletion of directory: {}", productSubPath);
      } catch (IOException e) {
        // Log error but don't necessarily fail the whole operation if directory delete
        // fails
        log.error("Could not delete product image directory {}: {}", productSubPath, e.getMessage());
      }
    }
    log.info("Finished deleting images for product ID: {}", product.getId());
  }

  @Override
  @Transactional // Add transactionality
  public void deleteImagesByIds(List<Long> imageIds, Long productId) throws IOException {
    log.info("Deleting images by IDs: {} for product ID: {}", imageIds, productId);
    if (imageIds == null || imageIds.isEmpty()) {
      log.warn("Image ID list is null or empty for product ID: {}. No images deleted.", productId);
      return;
    }

    List<ProductImage> imagesToDelete = productImageRepository.findAllById(imageIds);
    int deletedCount = 0;
    for (ProductImage image : imagesToDelete) {
      if (image.getProduct() == null || !image.getProduct().getId().equals(productId)) {
        log.error("Image ID {} does not belong to product ID {}. Skipping deletion.", image.getId(), productId);
        throw new UnauthorizedActionException(
            "Image ID " + image.getId() + " does not belong to product " + productId + ".");
      }
      deleteSingleProductImage(image);
      deletedCount++;
    }
    log.info("Finished deleting {} images by IDs for product ID: {}", deletedCount, productId);

    // This might be better handled in the ProductService update method after this
    // returns.
  }

  // Helper method to delete a single ProductImage entity and its associated file
  private void deleteSingleProductImage(ProductImage image) throws IOException {
    String relativePath = image.getImagePath();
    Long imageId = image.getId();
    log.debug("Attempting to delete image entity ID: {} with path: {}", imageId, relativePath);
    try {
      // Delete the physical file first
      if (relativePath != null && !relativePath.isBlank()) {
        try {
          fileStorageService.delete(relativePath);
          log.info("Successfully deleted image file: {}", relativePath);
        } catch (IOException e) {
          // Log the error but proceed to delete the entity anyway?
          // Or rethrow to indicate failure?
          log.error("Failed to delete image file {} for entity ID {}: {}", relativePath, imageId, e.getMessage(), e);
          // Rethrowing is safer to signal the operation partially failed.
          throw e;
        }
      } else {
        log.warn("ProductImage ID {} has no path associated. Cannot delete file.", imageId);
      }
      // Delete the entity from the database
      productImageRepository.delete(image);
      log.debug("Deleted ProductImage entity ID: {}", imageId);
    } catch (Exception e) { // Catch broader exceptions during entity deletion
      log.error("Failed to delete ProductImage entity ID {}: {}", imageId, e.getMessage(), e);
      // Rethrow as a runtime exception or specific exception if needed
      throw new RuntimeException("Failed to delete product image record for ID: " + imageId, e);
    }
  }
}
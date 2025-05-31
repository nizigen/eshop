package com.example.eshop.service.impl;

import com.example.eshop.dto.ProductSearchDto;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.exception.UnauthorizedActionException; // Need to create this exception
import com.example.eshop.model.*;
import com.example.eshop.repository.ProductRepository;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.repository.CategoryRepository;
import com.example.eshop.repository.ProductImageRepository;
import com.example.eshop.service.FileStorageService;
import com.example.eshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException; // Import IOException
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList; // Import ArrayList
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;

  // Helper method to check product ownership
  private Product findProductOwnedBySeller(Long productId, Seller seller) {
    return productRepository.findByIdAndSeller(productId, seller)
        .orElseThrow(() -> {
          log.warn("Seller {} attempted to access product {} which they do not own or doesn't exist.", seller.getId(),
              productId);
          return new ResourceNotFoundException("Product not found or not owned by seller.");
        });
  }

  // --- Public / General --- //

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findProductById(Long productId) {
    log.info("Finding product by ID: {}", productId);
    return productRepository.findById(productId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> findAllActiveProducts() {
    log.info("Finding all ACTIVE products");
    return productRepository.findByStatus(ProductStatus.ACTIVE);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Product> searchProducts(ProductSearchDto searchDto) {
    log.info("Searching products with criteria: {}", searchDto);

    // Create sort object based on sortBy parameter
    Sort sort = createSort(searchDto.getSortBy());

    // Create pageable with sort
    Pageable pageableWithSort = PageRequest.of(
        searchDto.getPage(),
        searchDto.getSize(),
        sort);

    return productRepository.findByStatusAndKeyword(
        ProductStatus.ACTIVE,
        searchDto.getKeyword(),
        pageableWithSort);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> findProductsByCategory(Long categoryId) {
    return productRepository.findByCategoryId(categoryId);
  }

  // --- Seller Specific --- //

  @Override
  @Transactional(readOnly = true)
  public List<Product> findProductsBySeller(Seller seller) {
    log.info("Finding products for seller ID: {}", seller.getId());
    return productRepository.findBySeller(seller);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findProductByIdAndSeller(Long productId, Seller seller) {
    log.info("Finding product ID: {} for seller ID: {}", productId, seller.getId());
    return productRepository.findByIdAndSeller(productId, seller);
  }

  @Override
  @Transactional
  public Product createProduct(Product product, List<MultipartFile> imageFiles, Seller seller) {
    log.info("Creating new product '{}' for seller ID: {}", product.getName(), seller.getId());
    product.setSeller(seller);
    product.setStatus(ProductStatus.PENDING_APPROVAL); // New products need approval
    // Clear potential ID from input
    product.setId(null);
    // Reset derived fields
    product.setSalesCount(0);
    product.setAverageRating(null);

    Product savedProduct = productRepository.save(product);
    log.info("Product saved with ID: {}. Now processing images.", savedProduct.getId());

    // Handle image uploads
    if (imageFiles != null && !imageFiles.isEmpty()) {
      boolean isFirstImage = true;
      for (MultipartFile file : imageFiles) {
        if (!file.isEmpty()) {
          try { // Add try-catch for IOException
            String imagePath = fileStorageService.store(file, "products/" + savedProduct.getId());
            ProductImage productImage = new ProductImage();
            productImage.setProduct(savedProduct);
            productImage.setImagePath(imagePath);
            productImage.setImageUrl("/uploads/" + imagePath);
            // Set the first uploaded image as primary by default
            productImage.setIsPrimary(isFirstImage);
            savedProduct.getImages().add(productImage);
            isFirstImage = false;
            log.debug("Added image {} to product {}", imagePath, savedProduct.getId());
          } catch (IOException e) {
            log.error("Failed to store image for product {}", savedProduct.getId(), e);
            // Rethrow as unchecked exception to propagate the error
            throw new RuntimeException("Failed to store image for product " + savedProduct.getId(), e);
          }
        }
      }
      // Save the product again to persist the new images via cascade
      savedProduct = productRepository.save(savedProduct);
      log.info("Product {} updated with {} images.", savedProduct.getId(), savedProduct.getImages().size());
    }
    return savedProduct;
  }

  @Override
  @Transactional
  public Product updateProduct(Long productId, Product productDetails, List<MultipartFile> newImageFiles,
      List<Long> imagesToDelete, Long primaryImageId, Seller seller) {
    log.info("Updating product ID: {} for seller ID: {}", productId, seller.getId());
    Product existingProduct = findProductOwnedBySeller(productId, seller);

    // Update fields from productDetails (be selective)
    existingProduct.setName(productDetails.getName());
    existingProduct.setCategory(productDetails.getCategory());
    existingProduct.setDescription(productDetails.getDescription());
    existingProduct.setOriginalPrice(productDetails.getOriginalPrice());
    existingProduct.setDiscountPrice(productDetails.getDiscountPrice());
    existingProduct.setSize(productDetails.getSize());
    existingProduct.setItemCondition(productDetails.getItemCondition());
    existingProduct.setQuantity(productDetails.getQuantity());
    existingProduct.setNegotiable(productDetails.getNegotiable());
    // Seller should not change status directly, only unlist/list
    // Re-set to pending if significant changes are made?
    if (existingProduct.getStatus() == ProductStatus.ACTIVE || existingProduct.getStatus() == ProductStatus.REJECTED
        || existingProduct.getStatus() == ProductStatus.UNLISTED) {
      existingProduct.setStatus(ProductStatus.PENDING_APPROVAL); // Require re-approval on edit? Or only if admin edits?
      log.info("Product {} status set to PENDING_APPROVAL after edit.", productId);
    }

    // Handle image deletions
    if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
      log.debug("Attempting to delete images with IDs: {} for product {}", imagesToDelete, productId);
      List<ProductImage> currentImages = new ArrayList<>(existingProduct.getImages()); // Avoid
                                                                                       // ConcurrentModificationException
      for (ProductImage img : currentImages) {
        if (imagesToDelete.contains(img.getId())) {
          log.info("Deleting image ID: {}, Path: {}", img.getId(), img.getImagePath());
          existingProduct.removeImage(img); // Removes from collection and DB via orphanRemoval
          try {
            fileStorageService.delete(img.getImagePath()); // <-- Correct method name: delete
            log.debug("Deleted image file: {}", img.getImagePath());
          } catch (IOException e) {
            // Log error but maybe don't fail the whole update?
            log.error("Failed to delete image file {} for product {}: {}", img.getImagePath(), productId,
                e.getMessage());
          }
        }
      }
    }

    // Handle new image uploads
    if (newImageFiles != null && !newImageFiles.isEmpty()) {
      log.debug("Adding new images for product {}", productId);
      for (MultipartFile file : newImageFiles) {
        if (!file.isEmpty()) {
          try {
            String imagePath = fileStorageService.store(file, "products/" + existingProduct.getId());
            ProductImage productImage = new ProductImage();
            productImage.setProduct(existingProduct);
            productImage.setImagePath(imagePath);
            // 修改 imageUrl 的设置方式，确保以 / 开头
            String imageUrl = "/uploads/" + imagePath;
            productImage.setImageUrl(imageUrl);
            // Temporarily set new images as non-primary, will handle explicit selection
            // later
            productImage.setIsPrimary(false);
            existingProduct.addImage(productImage);
            log.debug("Added new image {} to product {}", imagePath, existingProduct.getId());
          } catch (IOException e) {
            log.error("Failed to store new image for product {}", existingProduct.getId(), e);
            throw new RuntimeException("Failed to store new image for product " + existingProduct.getId(), e);
          }
        }
      }
    }

    // Handle primary image selection AFTER deletions and additions
    boolean primarySet = false;
    if (!existingProduct.getImages().isEmpty()) {
      if (primaryImageId != null) {
        // Attempt to set the explicitly selected primary image
        for (ProductImage img : existingProduct.getImages()) {
          boolean isSelectedPrimary = img.getId() != null && img.getId().equals(primaryImageId);
          img.setIsPrimary(isSelectedPrimary);
          if (isSelectedPrimary) {
            primarySet = true;
            log.debug("Set image ID {} as primary for product {}", primaryImageId, productId);
          }
        }
        // If the provided primaryImageId didn't match any existing/new images, log a
        // warning
        if (!primarySet) {
          log.warn("Provided primaryImageId {} did not match any image for product {}. Setting first image as primary.",
              primaryImageId, productId);
        }
      }

      // If no primary was explicitly set OR the selected one was invalid, set the
      // first image as primary
      if (!primarySet) {
        ProductImage firstImage = existingProduct.getImages().stream().findFirst().orElse(null);
        if (firstImage != null) {
          firstImage.setIsPrimary(true);
          primarySet = true;
          log.debug("Set first image (ID: {}) as primary for product {}", firstImage.getId(), productId);
          // Ensure others are not primary (in case of manual DB state)
          for (ProductImage img : existingProduct.getImages()) {
            if (img.getId() != firstImage.getId()) {
              img.setIsPrimary(false);
            }
          }
        } else {
          log.warn("Product {} has no images after update. Cannot set primary image.", productId);
        }
      }
    } else {
      log.warn("Product {} has no images after update. Cannot set primary image.", productId);
    }

    Product updatedProduct = productRepository.save(existingProduct);
    log.info("Product {} updated successfully.", updatedProduct.getId());
    return updatedProduct;
  }

  @Override
  @Transactional
  public void unlistProduct(Long productId, Seller seller) {
    log.info("Unlisting product ID: {} for seller ID: {}", productId, seller.getId());
    Product product = findProductOwnedBySeller(productId, seller);
    if (product.getStatus() == ProductStatus.ACTIVE || product.getStatus() == ProductStatus.PENDING_APPROVAL) { // Only
                                                                                                                // unlist
                                                                                                                // active/pending
      product.setStatus(ProductStatus.UNLISTED);
      productRepository.save(product);
      log.info("Product {} status set to UNLISTED.", productId);
    } else {
      log.warn("Product {} cannot be unlisted from status {}", productId, product.getStatus());
      // Optionally throw exception
    }
  }

  @Override
  @Transactional
  public void relistProduct(Long productId, Seller seller) {
    log.info("Re-listing product ID: {} for seller ID: {}", productId, seller.getId());
    Product product = findProductOwnedBySeller(productId, seller);
    if (product.getStatus() == ProductStatus.UNLISTED) { // Only re-list unlisted items
      product.setStatus(ProductStatus.PENDING_APPROVAL); // Go back to pending for admin check
      productRepository.save(product);
      log.info("Product {} status set to PENDING_APPROVAL for re-listing.", productId);
    } else {
      log.warn("Product {} cannot be re-listed from status {}", productId, product.getStatus());
      // Optionally throw exception
    }
  }

  // --- Admin Specific --- //

  @Override
  @Transactional(readOnly = true)
  public List<Product> findProductsByStatus(ProductStatus status) {
    log.info("Admin: Finding products with status: {}", status);
    return productRepository.findByStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> findPendingProducts() {
    log.info("Admin: Finding products with status: PENDING_APPROVAL");
    return productRepository.findByStatus(ProductStatus.PENDING_APPROVAL);
  }

  @Override
  @Transactional
  public void approveProduct(Long productId) {
    log.info("Admin: Approving product ID: {}", productId);
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    if (product.getStatus() == ProductStatus.PENDING_APPROVAL) {
      product.setStatus(ProductStatus.ACTIVE);
      productRepository.save(product);
      log.info("Product {} status set to ACTIVE.", productId);

    } else {
      log.warn("Admin: Product {} cannot be approved from status {}", productId, product.getStatus());
    }
  }

  @Override
  @Transactional
  public void rejectProduct(Long productId, String reason) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    product.setStatus(ProductStatus.REJECTED);
    // Store rejection reason in product description or a new field
    product.setDescription(product.getDescription() + "\nRejection reason: " + reason);
    productRepository.save(product);
  }

  @Override
  @Transactional
  public void deleteProduct(Long id) {
    Product product = getProductById(id);

    // Delete associated images from storage
    for (ProductImage image : product.getImages()) {
      try {
        fileStorageService.delete(image.getImagePath());
      } catch (IOException e) {
        log.error("Failed to delete image file: {}", image.getImagePath(), e);
      }
    }

    // Set status to UNLISTED
    product.setStatus(ProductStatus.UNLISTED);
    productRepository.save(product);
    log.info("Product {} has been marked as unlisted", id);
  }

  // Added method implementation
  @Override
  @Transactional(readOnly = true)
  public List<Product> findAllProducts() {
    log.info("Admin: Finding all products");
    return productRepository.findAll(); // Assuming ProductRepository extends JpaRepository or similar
  }

  @Override
  @Transactional(readOnly = true)
  public long countProductsBySeller(Seller seller) {
    log.info("Counting all products for seller ID: {}", seller.getId());
    return productRepository.countBySeller(seller);
  }

  @Override
  @Transactional(readOnly = true)
  public long countActiveProductsBySeller(Seller seller) {
    log.info("Counting active products for seller ID: {}", seller.getId());
    return productRepository.countBySellerAndStatus(seller, ProductStatus.ACTIVE);
  }

  @Override
  @Transactional(readOnly = true)
  public long countPendingProductsBySeller(Seller seller) {
    log.info("Counting pending products for seller ID: {}", seller.getId());
    return productRepository.countBySellerAndStatus(seller, ProductStatus.PENDING_APPROVAL);
  }

  @Override
  @Transactional(readOnly = true)
  public long countSoldProductsBySeller(Seller seller) {
    log.info("Counting sold out products for seller ID: {}", seller.getId());
    return productRepository.countBySellerAndStatus(seller, ProductStatus.SOLD_OUT);
  }

  @Override
  public Product getProductById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  @Override
  public Product getProductWithImagesAndReviews(Long id) {
    return productRepository.findByIdWithImagesAndReviews(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Product> getActiveProducts(Pageable pageable) {
    log.info("Getting active products with pageable: {}", pageable);
    return productRepository.findByStatusWithSort(ProductStatus.ACTIVE, pageable);
  }

  @Override
  public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
    return productRepository.findByStatusAndCategoryId(ProductStatus.ACTIVE, categoryId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Product> searchProducts(String keyword, Pageable pageable) {
    // Convert keyword to lowercase for case-insensitive search
    String searchKeyword = keyword != null ? keyword.toLowerCase() : "";
    return productRepository.searchProducts(ProductStatus.ACTIVE, searchKeyword, pageable);
  }

  @Override
  public Page<Product> getSellerProducts(Long sellerId, Pageable pageable) {
    Seller seller = (Seller) userRepository.findById(sellerId)
        .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));
    return productRepository.findBySeller(seller, pageable);
  }

  @Override
  @Transactional
  public Product createProduct(Product product, List<MultipartFile> images) {
    product.setStatus(ProductStatus.PENDING_APPROVAL);
    Product savedProduct = productRepository.save(product);

    if (images != null && !images.isEmpty()) {
      processProductImages(savedProduct, images);
    }

    return savedProduct;
  }

  @Override
  @Transactional
  public Product updateProduct(Long id, Product product, List<MultipartFile> newImages) {
    Product existingProduct = getProductById(id);

    // Update basic information
    existingProduct.setName(product.getName());
    existingProduct.setDescription(product.getDescription());
    existingProduct.setOriginalPrice(product.getOriginalPrice());
    existingProduct.setDiscountPrice(product.getDiscountPrice());
    existingProduct.setQuantity(product.getQuantity());
    existingProduct.setSize(product.getSize());
    existingProduct.setItemCondition(product.getItemCondition());
    existingProduct.setNegotiable(product.getNegotiable());
    existingProduct.setCategory(product.getCategory());

    if (existingProduct.getStatus() == ProductStatus.ACTIVE ||
        existingProduct.getStatus() == ProductStatus.REJECTED ||
        existingProduct.getStatus() == ProductStatus.UNLISTED) {
      existingProduct.setStatus(ProductStatus.PENDING_APPROVAL);
      log.info("Product {} status set to PENDING_APPROVAL after edit.", id);
    }

    // Process new images if provided
    if (newImages != null && !newImages.isEmpty()) {
      processProductImages(existingProduct, newImages);
    }

    return productRepository.save(existingProduct);
  }

  @Override
  public List<Product> getTopSellingProducts() {
    return productRepository.findTop8ByStatusOrderBySalesCountDesc(ProductStatus.ACTIVE);
  }

  @Override
  public List<Product> getNewArrivals() {
    return productRepository.findTop8ByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE);
  }

  @Override
  @Transactional
  public void updateProductRating(Long productId) {
    Product product = getProductById(productId);
    Set<Review> reviews = product.getReviews();

    if (!reviews.isEmpty()) {
      double averageRating = reviews.stream()
          .mapToInt(Review::getProductRating)
          .average()
          .orElse(0.0);

      product.setAverageRating(BigDecimal.valueOf(averageRating)
          .setScale(2, RoundingMode.HALF_UP));
      productRepository.save(product);
    }
  }

  private void processProductImages(Product product, List<MultipartFile> images) {
    for (int i = 0; i < images.size(); i++) {
      MultipartFile image = images.get(i);
      try {
        String imagePath = fileStorageService.store(image, "products/" + product.getId());
        String imageUrl = "/uploads/" + imagePath;

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImagePath(imagePath);
        productImage.setImageUrl(imageUrl);
        productImage.setIsPrimary(i == 0); // First image is primary

        product.getImages().add(productImage);
      } catch (IOException e) {
        throw new RuntimeException("Failed to store image for product " + product.getId(), e);
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Product> searchProductsWithSort(String keyword, String sortBy, Pageable pageable) {
    log.info("Searching products with keyword: '{}', sortBy: {}", keyword, sortBy);

    // Create sort object based on sortBy parameter
    Sort sort = createSort(sortBy);

    // Create new pageable with the sort
    Pageable pageableWithSort = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sort);

    return productRepository.searchProductsWithSort(
        ProductStatus.ACTIVE,
        keyword,
        pageableWithSort);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Product> getActiveProductsWithSort(String sortBy, Pageable pageable) {
    log.info("Getting active products with sortBy: {}", sortBy);

    // Create sort object based on sortBy parameter
    Sort sort = createSort(sortBy);

    // Create new pageable with the sort
    Pageable pageableWithSort = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sort);

    return productRepository.findActiveProductsWithSort(
        ProductStatus.ACTIVE,
        pageableWithSort);
  }

  /**
   * Create Sort object based on sortBy parameter
   */
  private Sort createSort(String sortBy) {
    if (sortBy == null || sortBy.trim().isEmpty() || "default".equals(sortBy)) {
      return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    switch (sortBy.toLowerCase().trim()) {
      case "price_asc":
        return Sort.by(Sort.Direction.ASC, "discountPrice");
      case "price_desc":
        return Sort.by(Sort.Direction.DESC, "discountPrice");
      case "sales":
        return Sort.by(Sort.Direction.DESC, "salesCount");
      case "rating":
        return Sort.by(Sort.Direction.DESC, "averageRating");
      default:
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> findLatestProducts(int limit) {
    return productRepository.findLatestProducts(ProductStatus.ACTIVE,
        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> findHotProducts(int limit) {
    return productRepository.findHotProducts(ProductStatus.ACTIVE,
        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "salesCount")));
  }

  @Override
  public Page<Product> findAllProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
  }

  @Override
  @Transactional
  public Product saveProduct(Product product) {
    return productRepository.save(product);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> findProductsBySeller(Long sellerId) {
    return productRepository.findBySellerId(sellerId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Product> findProductsBySeller(Long sellerId, Pageable pageable) {
    return productRepository.findBySellerId(sellerId, pageable);
  }

  @Override
  public long countAllProducts() {
    return productRepository.count();
  }

  @Override
  public long countProductsByStatus(ProductStatus status) {
    return productRepository.countByStatus(status);
  }

  @Override
  public List<Product> listProductsByStatus(String status) {
    try {
      ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
      return productRepository.findByStatus(productStatus);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid product status: " + status);
    }
  }

  @Override
  public List<Product> listAllProducts() {
    return productRepository.findAll();
  }

  @Override
  public Page<Product> findPendingProducts(Pageable pageable) {
    return productRepository.findByStatus(ProductStatus.PENDING_APPROVAL, pageable);
  }
}
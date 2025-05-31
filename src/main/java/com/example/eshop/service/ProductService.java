package com.example.eshop.service;

import com.example.eshop.dto.ProductSearchDto;
import com.example.eshop.model.Product;
import com.example.eshop.model.Seller;
import com.example.eshop.model.ProductStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {

  // --- Public / General --- //
  Optional<Product> findProductById(Long productId);

  List<Product> findAllActiveProducts();

  List<Product> findProductsByCategory(Long categoryId);

  // --- Seller Specific --- //
  List<Product> findProductsBySeller(Seller seller);

  Optional<Product> findProductByIdAndSeller(Long productId, Seller seller);

  Product createProduct(Product product, List<MultipartFile> imageFiles, Seller seller);

  Product updateProduct(Long productId, Product productDetails, List<MultipartFile> newImageFiles,
      List<Long> imagesToDelete, Long primaryImageId, Seller seller);

  void unlistProduct(Long productId, Seller seller);

  void relistProduct(Long productId, Seller seller);

  // --- Admin Specific --- //
  List<Product> findProductsByStatus(ProductStatus status);

  List<Product> findPendingProducts();

  void approveProduct(Long productId);

  /**
   * Reject a product with a reason
   * 
   * @param productId the ID of the product to reject
   * @param reason    the reason for rejection
   */
  void rejectProduct(Long productId, String reason);

  void deleteProduct(Long productId);

  List<Product> findAllProducts();

  // Seller dashboard statistics
  long countProductsBySeller(Seller seller);

  long countActiveProductsBySeller(Seller seller);

  long countPendingProductsBySeller(Seller seller);

  long countSoldProductsBySeller(Seller seller);

  Page<Product> searchProducts(ProductSearchDto searchDto);

  Product getProductById(Long id);

  Product getProductWithImagesAndReviews(Long id);

  Page<Product> getActiveProducts(Pageable pageable);

  Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);

  Page<Product> searchProducts(String keyword, Pageable pageable);

  Page<Product> getSellerProducts(Long sellerId, Pageable pageable);

  Product createProduct(Product product, List<MultipartFile> images);

  Product updateProduct(Long id, Product product, List<MultipartFile> newImages);

  List<Product> getTopSellingProducts();

  List<Product> getNewArrivals();

  void updateProductRating(Long productId);

  Page<Product> searchProductsWithSort(String keyword, String sortBy, Pageable pageable);

  Page<Product> getActiveProductsWithSort(String sortBy, Pageable pageable);

  List<Product> findLatestProducts(int limit);

  List<Product> findHotProducts(int limit);

  Product saveProduct(Product product);

  List<Product> findProductsBySeller(Long sellerId);

  Page<Product> findProductsBySeller(Long sellerId, Pageable pageable);

  long countAllProducts();

  long countProductsByStatus(ProductStatus status);

  /**
   * List products by status
   * 
   * @param status the status to filter by
   * @return list of products with the specified status
   */
  List<Product> listProductsByStatus(String status);

  /**
   * List all products
   * 
   * @return list of all products
   */
  List<Product> listAllProducts();

  /**
   * Find all products with pagination
   * 
   * @param pageable the pagination information
   * @return page of products
   */
  Page<Product> findAllProducts(Pageable pageable);

  /**
   * Find pending products with pagination
   * 
   * @param pageable the pagination information
   * @return page of pending products
   */
  Page<Product> findPendingProducts(Pageable pageable);
}
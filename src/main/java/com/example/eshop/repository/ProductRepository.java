package com.example.eshop.repository;

import com.example.eshop.model.Product;
import com.example.eshop.model.Seller;
import com.example.eshop.model.ProductStatus;
import com.example.eshop.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Finds all products associated with a specific seller.
   *
   * @param seller The seller user.
   * @return A list of products belonging to the seller.
   */
  List<Product> findBySeller(Seller seller);

  /**
   * Finds a product by its ID and the seller who owns it.
   * Useful for ensuring a seller can only modify their own products.
   *
   * @param id     The product ID.
   * @param seller The seller user.
   * @return An Optional containing the product if found and owned by the seller,
   *         otherwise empty.
   */
  Optional<Product> findByIdAndSeller(Long id, Seller seller);

  /**
   * Finds all products with a specific status.
   * Useful for admin approval queues or filtering.
   *
   * @param status The status to filter by.
   * @return A list of products matching the status.
   */
  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.seller s LEFT JOIN FETCH s.sellerDetails WHERE p.status = :status")
  List<Product> findByStatus(@Param("status") ProductStatus status);

  /**
   * Finds all products in a specific category with a specific status.
   *
   * @param categoryId The category ID.
   * @param status     The product status.
   * @return A list of products matching the category and status.
   */
  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.seller s LEFT JOIN FETCH s.sellerDetails WHERE p.category.id = :categoryId AND p.status = :status")
  List<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") ProductStatus status);

  /**
   * Counts all products by a specific seller.
   *
   * @param seller The seller user.
   * @return The count of products.
   */
  long countBySeller(Seller seller);

  /**
   * Counts all products by a specific seller and status.
   *
   * @param seller The seller user.
   * @param status The product status.
   * @return The count of products.
   */
  long countBySellerAndStatus(Seller seller, ProductStatus status);

  /**
   * Counts all products by a specific seller and quantity.
   *
   * @param seller   The seller user.
   * @param quantity The product quantity.
   * @return The count of products.
   */
  long countBySellerAndQuantity(Seller seller, Integer quantity);

  // Add other specific query methods if needed later, e.g.,
  // List<Product> findByNameContainingIgnoreCaseAndStatus(String name,
  // ProductStatus status);

  // 基本搜索
  Page<Product> findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
      ProductStatus status1, String name,
      ProductStatus status2, String description,
      Pageable pageable);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.seller s LEFT JOIN FETCH s.sellerDetails")
  @NonNull
  List<Product> findAll();

  Page<Product> findByStatus(ProductStatus status, Pageable pageable);

  Page<Product> findByStatusAndCategoryId(ProductStatus status, Long categoryId, Pageable pageable);

  Page<Product> findBySeller(Seller seller, Pageable pageable);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
  Optional<Product> findByIdWithImages(@Param("id") Long id);

  @Query("SELECT p FROM Product p " +
      "LEFT JOIN FETCH p.images " +
      "LEFT JOIN FETCH p.reviews r " +
      "LEFT JOIN FETCH r.user " +
      "LEFT JOIN FETCH p.seller s " +
      "LEFT JOIN FETCH s.sellerDetails " +
      "WHERE p.id = :id")
  Optional<Product> findByIdWithImagesAndReviews(@Param("id") Long id);

  List<Product> findTop8ByStatusOrderBySalesCountDesc(ProductStatus status);

  List<Product> findTop8ByStatusOrderByCreatedAtDesc(ProductStatus status);

  /**
   * Enhanced search query with better performance and flexibility
   */
  @Query("SELECT p FROM Product p " +
      "WHERE p.status = :status " +
      "AND (p.name LIKE CONCAT('%', :keyword, '%') " +
      "OR p.description LIKE CONCAT('%', :keyword, '%'))")
  Page<Product> searchProductsWithSort(
      @Param("status") ProductStatus status,
      @Param("keyword") String keyword,
      Pageable pageable);

  /**
   * Find active products with sorting options
   */
  @Query("SELECT p FROM Product p WHERE p.status = :status")
  Page<Product> findActiveProductsWithSort(
      @Param("status") ProductStatus status,
      Pageable pageable);

  /**
   * Search products with custom query to avoid type conversion issues
   */
  @Query("SELECT p FROM Product p " +
      "WHERE p.status = :status " +
      "AND (CAST(p.name as string) LIKE %:keyword% " +
      "OR CAST(p.description as string) LIKE %:keyword%)")
  Page<Product> findByStatusAndKeyword(
      @Param("status") ProductStatus status,
      @Param("keyword") String keyword,
      Pageable pageable);

  /**
   * Find active products with custom query
   */
  @Query("SELECT p FROM Product p WHERE p.status = :status")
  Page<Product> findByStatusWithSort(
      @Param("status") ProductStatus status,
      Pageable pageable);

  List<Product> findByCategoryId(Long categoryId);

  Long countByCategory(Category category);

  List<Product> findBySellerId(Long sellerId);

  Page<Product> findBySellerId(Long sellerId, Pageable pageable);

  @Query("SELECT p FROM Product p WHERE p.status = :status")
  Page<Product> findAllActiveProducts(@Param("status") ProductStatus status, Pageable pageable);

  @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdAt DESC")
  List<Product> findLatestProducts(@Param("status") ProductStatus status, Pageable pageable);

  @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.salesCount DESC")
  List<Product> findHotProducts(@Param("status") ProductStatus status, Pageable pageable);

  @Query("SELECT p FROM Product p WHERE p.status = :status AND " +
      "(p.name LIKE CONCAT('%', :keyword, '%') OR " +
      "p.description LIKE CONCAT('%', :keyword, '%'))")
  Page<Product> searchProducts(@Param("status") ProductStatus status, @Param("keyword") String keyword,
      Pageable pageable);

  long countByStatus(ProductStatus status);
}
package com.example.eshop.repository;

import com.example.eshop.model.Review;
import com.example.eshop.model.User;
import com.example.eshop.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  // Find reviews for a specific product, ordered by creation date (newest first)
  List<Review> findByProduct_IdOrderByCreatedAtDesc(Long productId);

  // Find reviews submitted by a specific user
  List<Review> findByUser_IdOrderByCreatedAtDesc(Long userId);

  // Check if a review exists for a specific order item (to prevent duplicates)
  boolean existsByOrderItem_Id(Long orderItemId);

  // Optional: Find reviews for a specific seller
  // List<Review> findBySeller_IdOrderByCreatedAtDesc(Long sellerId);

  Page<Review> findByProductId(Long productId, Pageable pageable);

  @Query("SELECT r FROM Review r WHERE r.product.seller.id = :sellerId " +
      "AND (:productName IS NULL OR LOWER(r.product.name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
      "AND (:rating IS NULL OR r.productRating = :rating) " +
      "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
      "AND (:endDate IS NULL OR r.createdAt <= :endDate)")
  Page<Review> findReviewsBySeller(
      @Param("sellerId") Long sellerId,
      @Param("productName") String productName,
      @Param("rating") Integer rating,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  @Query("SELECT r FROM Review r WHERE r.product.seller.id = :sellerId")
  List<Review> findAllByProductSellerId(@Param("sellerId") Long sellerId);

  List<Review> findByProductSellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.product.seller.id = :sellerId")
  long countBySellerId(@Param("sellerId") Long sellerId);

  List<Review> findByProductId(Long productId);

  List<Review> findByProductSellerId(Long sellerId);

  List<Review> findByUserId(Long userId);

  Optional<Review> findByOrderItemId(Long orderItemId);

  boolean existsByUserIdAndOrderItemId(Long userId, Long orderItemId);

  @Query("SELECT AVG(r.productRating) FROM Review r WHERE r.product.id = :productId")
  double calculateAverageProductRating(@Param("productId") Long productId);

  @Query("SELECT COALESCE(AVG(r.sellerServiceRating), 0.0) FROM Review r WHERE r.product.seller.id = :sellerId")
  double calculateAverageServiceRating(@Param("sellerId") Long sellerId);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.product.seller.id = :sellerId AND r.sellerServiceRating >= 4")
  long countPositiveReviews(@Param("sellerId") Long sellerId);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.product.seller.id = :sellerId")
  long countTotalReviews(@Param("sellerId") Long sellerId);

  Page<Review> findByProductSellerId(Long sellerId, Pageable pageable);

  Review findByUserAndOrderItem(User user, OrderItem orderItem);
}
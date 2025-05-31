package com.example.eshop.service;

import com.example.eshop.dto.ReviewDto; // Assuming a DTO for input
import com.example.eshop.dto.ReviewRequest;
import com.example.eshop.model.Review;
import com.example.eshop.model.User;
import com.example.eshop.model.Product;
import com.example.eshop.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewService {

  /**
   * Adds a review for a specific order item.
   * Ensures the user submitting the review is the one who purchased the item.
   *
   * @param reviewDto   The review data (rating, comment).
   * @param orderItemId The ID of the order item being reviewed.
   * @param buyer       The user submitting the review.
   * @return The saved Review object.
   * @throws // Add specific exceptions for validation, not found, already
   *            reviewed, unauthorized
   */
  Review addReview(ReviewDto reviewDto, Long orderItemId, User buyer);

  /**
   * Finds all reviews for a given product, ordered by creation date descending.
   *
   * @param productId The ID of the product.
   * @return A list of reviews for the product.
   */
  List<Review> findReviewsByProduct(Long productId);

  /**
   * Calculates and updates the average product rating for a given product.
   * Should be called after adding or potentially deleting a review.
   *
   * @param productId The ID of the product to update.
   */
  void updateAverageRating(Long productId);

  Review save(Review review);

  Page<Review> findReviewsByProduct(Long productId, Pageable pageable);

  Page<Review> findReviewsBySeller(Long sellerId, String keyword, Integer rating, String sortBy, String sortDirection,
      Pageable pageable);

  double calculateAverageRating(Long sellerId);

  Map<Integer, Long> calculateRatingDistribution(Long sellerId);

  /**
   * Adds a seller's reply to a review.
   *
   * @param reviewId     The ID of the review to reply to.
   * @param sellerId     The ID of the seller adding the reply.
   * @param replyContent The content of the reply.
   * @throws RuntimeException if the review doesn't exist, the seller is not
   *                          authorized,
   *                          or the review already has a reply.
   */
  void addReplyToReview(Long reviewId, Long sellerId, String reply);

  void deleteReview(Long reviewId);

  // Seller dashboard statistics
  Page<Review> findRecentReviewsBySeller(Long sellerId, int limit);

  long countReviewsBySeller(Long sellerId);

  double calculatePositiveRatePercent(Long sellerId);

  double calculateAverageServiceRating(Long sellerId);

  // 创建评价
  Review createReview(Long orderItemId, ReviewRequest request, UserDetails userDetails);

  // 获取商品的所有评价
  List<Review> getProductReviews(Long productId);

  // 获取卖家的所有评价
  List<Review> getSellerReviews(Long sellerId);

  // 获取用户的评价历史
  List<Review> getUserReviews(Long userId);

  // 获取订单项的评价
  Optional<Review> getOrderItemReview(Long orderItemId);

  // 更新评价
  Review updateReview(Long reviewId, Review review);

  // 卖家回复评价
  Review replyToReview(Long reviewId, String reply);

  // 计算商品的平均评分
  double calculateProductAverageRating(Long productId);

  // 计算卖家的平均服务评分
  double calculateSellerAverageRating(Long sellerId);

  // 检查用户是否已评价订单项
  boolean hasUserReviewedOrderItem(Long userId, Long orderItemId);

  // 更新商品评分
  void updateProductRating(Long productId);

  // 更新卖家评分
  void updateSellerRating(Long sellerId);

  // 通过用户名获取用户评价
  List<Review> getUserReviews(String username);

  // 通过用户名获取商家评价
  Page<Review> findReviewsBySeller(String username, String productName, Integer rating,
      String startDate, String endDate, Pageable pageable);

  // 通过用户名获取商家评价统计
  long countReviewsBySeller(String username);

  double calculateAverageRating(String username);

  double calculatePositiveRatePercent(String username);

  Map<Integer, Long> calculateRatingDistribution(String username);
}
package com.example.eshop.service.impl;

import com.example.eshop.dto.ReviewDto;
import com.example.eshop.dto.ReviewRequest;
import com.example.eshop.model.Review;
import com.example.eshop.model.User;
import com.example.eshop.model.Product;
import com.example.eshop.model.OrderItem;
import com.example.eshop.model.Seller;
import com.example.eshop.model.Order;
import com.example.eshop.model.SellerBuyerReview;
import com.example.eshop.repository.ReviewRepository;
import com.example.eshop.repository.ProductRepository;
import com.example.eshop.repository.OrderItemRepository;
import com.example.eshop.repository.SellerDetailsRepository;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.service.ReviewService;
import com.example.eshop.service.SellerBuyerReviewService;
import com.example.eshop.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.eshop.model.OrderStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;
  private final OrderItemRepository orderItemRepository;
  private final SellerDetailsRepository sellerDetailsRepository;
  private final UserRepository userRepository;
  private final SellerBuyerReviewService sellerBuyerReviewService;

  @Override
  @Transactional
  public Review createReview(Long orderItemId, ReviewRequest request, UserDetails userDetails) {
    OrderItem orderItem = orderItemRepository.findById(orderItemId)
        .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

    // 检查订单状态
    if (orderItem.getOrder().getStatus() != OrderStatus.COMPLETED) {
      throw new IllegalStateException("Can only review completed orders");
    }

    // 检查是否是订单的买家
    User user = userRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (!orderItem.getOrder().getUser().getId().equals(user.getId())) {
      throw new IllegalStateException("Only the buyer can review this order");
    }

    // 查找是否已存在评价
    Review existingReview = reviewRepository.findByUserAndOrderItem(user, orderItem);

    if (existingReview != null) {
      // 如果已存在评价，更新它
      existingReview.setProductRating(request.getProductRating());
      existingReview.setProductComment(request.getProductComment());
      existingReview.setSellerServiceRating(request.getServiceRating());
      existingReview.setSellerServiceComment(request.getServiceComment());
      return reviewRepository.save(existingReview);
    }

    // 创建新评价
    Review review = new Review();
    review.setUser(user);
    review.setOrderItem(orderItem);
    review.setProduct(orderItem.getProduct());
    review.setSeller((User) orderItem.getSeller()); // 将 Seller 转换为 User
    review.setProductRating(request.getProductRating());
    review.setProductComment(request.getProductComment());
    review.setSellerServiceRating(request.getServiceRating());
    review.setSellerServiceComment(request.getServiceComment());
    review.setCreatedAt(LocalDateTime.now());

    Review savedReview = reviewRepository.save(review);

    // 更新商品和卖家的平均评分
    updateProductRating(orderItem.getProduct().getId());
    updateSellerRating(orderItem.getSeller().getId());

    return savedReview;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Review> getProductReviews(Long productId) {
    return reviewRepository.findByProductId(productId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Review> getSellerReviews(Long sellerId) {
    return reviewRepository.findByProductSellerId(sellerId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Review> getUserReviews(Long userId) {
    return reviewRepository.findByUserId(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Review> getUserReviews(String username) {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return getUserReviews(user.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Review> getOrderItemReview(Long orderItemId) {
    return reviewRepository.findByOrderItemId(orderItemId);
  }

  @Override
  public Review updateReview(Long reviewId, Review updatedReview) {
    Review existingReview = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

    // 只允许更新评分和评论内容
    existingReview.setProductRating(updatedReview.getProductRating());
    existingReview.setProductComment(updatedReview.getProductComment());
    existingReview.setSellerServiceRating(updatedReview.getSellerServiceRating());
    existingReview.setSellerServiceComment(updatedReview.getSellerServiceComment());

    Review savedReview = reviewRepository.save(existingReview);

    // 更新商品和卖家的平均评分
    updateProductRating(existingReview.getProduct().getId());
    updateSellerRating(existingReview.getSeller().getId());

    return savedReview;
  }

  @Override
  public void deleteReview(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

    reviewRepository.delete(review);

    // 更新商品和卖家的平均评分
    updateProductRating(review.getProduct().getId());
    updateSellerRating(review.getSeller().getId());
  }

  @Override
  public Review replyToReview(Long reviewId, String reply) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

    review.setSellerReply(reply);
    review.setSellerReplyTime(LocalDateTime.now());

    return reviewRepository.save(review);
  }

  @Override
  @Transactional(readOnly = true)
  public double calculateProductAverageRating(Long productId) {
    return reviewRepository.calculateAverageProductRating(productId);
  }

  @Override
  @Transactional(readOnly = true)
  public double calculateSellerAverageRating(Long sellerId) {
    return reviewRepository.calculateAverageServiceRating(sellerId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasUserReviewedOrderItem(Long userId, Long orderItemId) {
    return reviewRepository.existsByUserIdAndOrderItemId(userId, orderItemId);
  }

  @Override
  public Review addReview(ReviewDto reviewDto, Long orderItemId, User buyer) {
    OrderItem orderItem = orderItemRepository.findById(orderItemId)
        .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

    // 验证买家身份
    if (!orderItem.getOrder().getUser().getId().equals(buyer.getId())) {
      throw new IllegalStateException("Only the buyer can review this order item");
    }

    // 检查是否已评价
    if (hasUserReviewedOrderItem(buyer.getId(), orderItemId)) {
      throw new IllegalStateException("You have already reviewed this order item");
    }

    Review review = new Review();
    review.setOrderItem(orderItem);
    review.setUser(buyer);
    review.setProduct(orderItem.getProduct());
    review.setSeller(orderItem.getSeller());
    review.setProductRating(reviewDto.getProductRating());
    review.setProductComment(reviewDto.getProductComment());
    review.setSellerServiceRating(reviewDto.getSellerServiceRating());
    review.setSellerServiceComment(reviewDto.getSellerServiceComment());
    review.setCreatedAt(LocalDateTime.now());

    Review savedReview = reviewRepository.save(review);
    updateProductRating(orderItem.getProduct().getId());
    return savedReview;
  }

  @Override
  public List<Review> findReviewsByProduct(Long productId) {
    return reviewRepository.findByProductId(productId);
  }

  @Override
  public Page<Review> findReviewsByProduct(Long productId, Pageable pageable) {
    return reviewRepository.findByProductId(productId, pageable);
  }

  @Override
  public Page<Review> findReviewsBySeller(Long sellerId, String productName, Integer rating,
      String startDate, String endDate, Pageable pageable) {
    Page<Review> page = reviewRepository.findReviewsBySeller(sellerId, productName, rating,
        startDate != null ? LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME) : null,
        endDate != null ? LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME) : null,
        pageable);
    // 组装sellerBuyerReview
    for (Review review : page.getContent()) {
      Order order = review.getOrderItem().getOrder();
      sellerBuyerReviewService.findByOrderAndSeller(order, review.getSeller())
          .ifPresent(sbr -> review.getClass().getDeclaredFields()); // 仅触发懒加载
    }
    return page;
  }

  @Override
  public Page<Review> findReviewsBySeller(String username, String productName, Integer rating,
      String startDate, String endDate, Pageable pageable) {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return findReviewsBySeller(user.getId(), productName, rating, startDate, endDate, pageable);
  }

  @Override
  public double calculateAverageRating(Long sellerId) {
    return reviewRepository.calculateAverageServiceRating(sellerId);
  }

  @Override
  public double calculateAverageRating(String username) {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return calculateAverageRating(user.getId());
  }

  @Override
  public Map<Integer, Long> calculateRatingDistribution(Long sellerId) {
    List<Review> reviews = reviewRepository.findByProductSellerId(sellerId);
    Map<Integer, Long> distribution = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
      final int rating = i;
      long count = reviews.stream()
          .filter(r -> r.getProductRating() == rating)
          .count();
      distribution.put(rating, count);
    }
    return distribution;
  }

  @Override
  public Map<Integer, Long> calculateRatingDistribution(String username) {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return calculateRatingDistribution(user.getId());
  }

  @Override
  public double calculatePositiveRatePercent(Long sellerId) {
    long positiveReviews = reviewRepository.countPositiveReviews(sellerId);
    long totalReviews = reviewRepository.countTotalReviews(sellerId);
    return totalReviews > 0 ? (double) positiveReviews / totalReviews * 100 : 0;
  }

  @Override
  public double calculatePositiveRatePercent(String username) {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return calculatePositiveRatePercent(user.getId());
  }

  @Override
  public void addReplyToReview(Long reviewId, Long sellerId, String replyContent) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

    if (!review.getSeller().getId().equals(sellerId)) {
      throw new IllegalStateException("Only the seller can reply to this review");
    }

    if (review.getSellerReply() != null) {
      throw new IllegalStateException("This review already has a reply");
    }

    review.setSellerReply(replyContent);
    review.setSellerReplyTime(LocalDateTime.now());
    reviewRepository.save(review);
  }

  @Override
  public Page<Review> findRecentReviewsBySeller(Long sellerId, int limit) {
    Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
    return reviewRepository.findByProductSellerId(sellerId, pageable);
  }

  @Override
  public void updateProductRating(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    Double averageRating = reviewRepository.calculateAverageProductRating(productId);
    if (averageRating != null) {
      product.setAverageRating(java.math.BigDecimal.valueOf(averageRating));
      productRepository.save(product);
    }
  }

  @Override
  public void updateSellerRating(Long sellerId) {
    Double averageRating = reviewRepository.calculateAverageServiceRating(sellerId);
    if (averageRating != null) {
      sellerDetailsRepository.updateServiceRating(sellerId, averageRating);
    }
  }

  @Override
  public Review save(Review review) {
    return reviewRepository.save(review);
  }

  @Override
  public long countReviewsBySeller(Long sellerId) {
    return reviewRepository.countBySellerId(sellerId);
  }

  @Override
  public long countReviewsBySeller(String username) {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return countReviewsBySeller(user.getId());
  }

  @Override
  public double calculateAverageServiceRating(Long sellerId) {
    return reviewRepository.calculateAverageServiceRating(sellerId);
  }

  @Override
  public void updateAverageRating(Long productId) {
    updateProductRating(productId);
  }
}
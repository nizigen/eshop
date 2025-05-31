package com.example.eshop.controller;

import com.example.eshop.dto.ReviewRequest;
import com.example.eshop.model.Review;
import com.example.eshop.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping("/{orderItemId}/review")
  public ResponseEntity<?> createReview(
      @PathVariable Long orderItemId,
      @RequestBody ReviewRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    Review review = reviewService.createReview(orderItemId, request, userDetails);
    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "评价提交成功",
        "data", review));
  }

  @GetMapping("/products/{productId}/reviews")
  public ResponseEntity<?> getProductReviews(@PathVariable Long productId) {
    List<Review> reviews = reviewService.getProductReviews(productId);
    return ResponseEntity.ok(Map.of(
        "success", true,
        "data", reviews));
  }

  @GetMapping("/sellers/{sellerId}/reviews")
  public ResponseEntity<?> getSellerReviews(@PathVariable Long sellerId) {
    List<Review> reviews = reviewService.getSellerReviews(sellerId);
    return ResponseEntity.ok(Map.of(
        "success", true,
        "data", reviews));
  }

  @GetMapping("/orders/{orderId}/review")
  public ResponseEntity<?> getOrderReview(@PathVariable Long orderId) {
    return reviewService.getOrderItemReview(orderId)
        .map(review -> ResponseEntity.ok(Map.of(
            "success", true,
            "data", review)))
        .orElse(ResponseEntity.ok(Map.of(
            "success", true,
            "data", null)));
  }

  @GetMapping("/users/me/reviews")
  public ResponseEntity<?> getMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
    List<Review> reviews = reviewService.getUserReviews(userDetails.getUsername());
    return ResponseEntity.ok(Map.of(
        "success", true,
        "data", reviews));
  }

  @GetMapping("/sellers/me/reviews")
  public ResponseEntity<?> getMySellerReviews(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) String productName,
      @RequestParam(required = false) Integer rating,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Review> reviews = reviewService.findReviewsBySeller(
        userDetails.getUsername(), productName, rating, startDate, endDate, pageable);
    return ResponseEntity.ok(Map.of(
        "success", true,
        "data", reviews));
  }

  @GetMapping("/sellers/me/reviews/stats")
  public ResponseEntity<?> getMySellerReviewStats(@AuthenticationPrincipal UserDetails userDetails) {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalReviews", reviewService.countReviewsBySeller(userDetails.getUsername()));
    stats.put("averageRating", reviewService.calculateAverageRating(userDetails.getUsername()));
    stats.put("positiveRate", reviewService.calculatePositiveRatePercent(userDetails.getUsername()));
    stats.put("ratingDistribution", reviewService.calculateRatingDistribution(userDetails.getUsername()));
    return ResponseEntity.ok(Map.of(
        "success", true,
        "data", stats));
  }

  @PostMapping("/orders/{orderId}/review")
  public ResponseEntity<?> createOrderReview(
      @PathVariable Long orderId,
      @RequestBody ReviewRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    // 兼容orderId到orderItemId的映射，假设一个订单只有一个订单项，或前端传递的orderId实际为orderItemId
    // 推荐前端传orderItemId，否则这里需查找orderItem
    return createReview(orderId, request, userDetails);
  }
}
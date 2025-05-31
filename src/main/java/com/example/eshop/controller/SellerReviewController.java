package com.example.eshop.controller;

import com.example.eshop.model.Review;
import com.example.eshop.model.User;
import com.example.eshop.model.SellerBuyerReview;
import com.example.eshop.model.Order;
import com.example.eshop.service.ReviewService;
import com.example.eshop.service.UserService;
import com.example.eshop.repository.OrderRepository;
import com.example.eshop.service.SellerBuyerReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/seller/reviews")
public class SellerReviewController {

  private final ReviewService reviewService;
  private final UserService userService;
  private final SellerBuyerReviewService sellerBuyerReviewService;
  private final OrderRepository orderRepository;

  @Autowired
  public SellerReviewController(ReviewService reviewService, UserService userService,
      SellerBuyerReviewService sellerBuyerReviewService, OrderRepository orderRepository) {
    this.reviewService = reviewService;
    this.userService = userService;
    this.sellerBuyerReviewService = sellerBuyerReviewService;
    this.orderRepository = orderRepository;
  }

  @GetMapping
  public String listReviews(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String productName,
      @RequestParam(required = false) Integer rating,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      Model model,
      Authentication authentication) {

    User seller = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    Page<Review> reviewsPage = reviewService.findReviewsBySeller(
        seller.getId(),
        productName,
        rating,
        startDate,
        endDate,
        PageRequest.of(page, size));

    double averageRating = reviewService.calculateAverageRating(seller.getId());
    long totalReviews = reviewsPage.getTotalElements();
    Map<Integer, Long> ratingDistribution = reviewService.calculateRatingDistribution(seller.getId());

    List<Review> reviews = reviewsPage.getContent();

    // 组装 orderId -> SellerBuyerReview 的 Map
    Map<Long, SellerBuyerReview> reviewIdToSellerBuyerReview = new java.util.HashMap<>();
    for (Review review : reviews) {
      Order order = review.getOrderItem().getOrder();
      sellerBuyerReviewService.findByOrderAndSeller(order, seller)
          .ifPresent(sbr -> reviewIdToSellerBuyerReview.put(order.getId(), sbr));
    }

    // 统计好评率（4星及以上）
    long positiveCount = reviews.stream()
        .filter(r -> r.getProductRating() != null && r.getProductRating() >= 4)
        .count();
    double positiveRatePercent = totalReviews > 0 ? (positiveCount * 100.0 / totalReviews) : 0;

    // 统计待回复数量（未回复的评价）
    long pendingReplyCount = reviews.stream()
        .filter(r -> r.getSellerReply() == null || r.getSellerReply().isEmpty())
        .count();

    model.addAttribute("reviews", reviews);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", reviewsPage.getTotalPages());
    model.addAttribute("totalReviews", totalReviews);
    model.addAttribute("averageRating", averageRating);
    model.addAttribute("ratingDistribution", ratingDistribution);
    model.addAttribute("pageSize", size);
    model.addAttribute("reviewIdToSellerBuyerReview", reviewIdToSellerBuyerReview);
    model.addAttribute("positiveRatePercent", String.format("%.2f%%", positiveRatePercent));
    model.addAttribute("pendingReplyCount", pendingReplyCount);

    return "seller/reviews";
  }

  @PostMapping("/{reviewId}/reply")
  public Object replyToReview(
      @PathVariable Long reviewId,
      @RequestParam(required = false) String replyContent,
      @RequestParam(required = false) Long sellerId,
      @RequestBody(required = false) String replyBody,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {

    String reply = replyContent != null ? replyContent : replyBody;
    if (reply == null) {
      throw new IllegalArgumentException("Reply content cannot be null");
    }

    // 如果是API请求（没有authentication）
    if (authentication == null && sellerId != null) {
      reviewService.addReplyToReview(reviewId, sellerId, reply);
      return ResponseEntity.ok().build();
    }

    // 如果是Web请求
    User seller = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    try {
      reviewService.addReplyToReview(reviewId, seller.getId(), reply);
      redirectAttributes.addFlashAttribute("success", "回复已添加");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "无法添加回复: " + e.getMessage());
    }

    return "redirect:/seller/reviews";
  }

  @GetMapping("/{sellerId}")
  public ResponseEntity<Page<Review>> getSellerReviews(
      @PathVariable Long sellerId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer rating,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDirection,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Sort.Direction direction = Sort.Direction.fromString(sortDirection);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    Page<Review> reviews = reviewService.findReviewsBySeller(sellerId, keyword, rating, sortBy, sortDirection,
        pageable);
    return ResponseEntity.ok(reviews);
  }

  @GetMapping("/{sellerId}/stats")
  public ResponseEntity<Map<String, Object>> getSellerReviewStats(@PathVariable Long sellerId) {
    double averageRating = reviewService.calculateAverageRating(sellerId);
    double positiveRate = reviewService.calculatePositiveRatePercent(sellerId);
    Map<Integer, Long> ratingDistribution = reviewService.calculateRatingDistribution(sellerId);
    long totalReviews = reviewService.countReviewsBySeller(sellerId);

    Map<String, Object> stats = Map.of(
        "averageRating", averageRating,
        "positiveRate", positiveRate,
        "ratingDistribution", ratingDistribution,
        "totalReviews", totalReviews);

    return ResponseEntity.ok(stats);
  }

  @GetMapping("/{sellerId}/recent")
  public ResponseEntity<Page<Review>> getRecentReviews(
      @PathVariable Long sellerId,
      @RequestParam(defaultValue = "5") int limit) {
    Page<Review> reviews = reviewService.findRecentReviewsBySeller(sellerId, limit);
    return ResponseEntity.ok(reviews);
  }

  @PostMapping("/buyer")
  public String reviewBuyer(@RequestParam Long orderId,
      @RequestParam Boolean isPositive,
      @RequestParam String comment,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    User seller = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found"));
    User buyer = order.getUser();
    try {
      sellerBuyerReviewService.addReview(order, seller, buyer, isPositive, comment);
      redirectAttributes.addFlashAttribute("success", "回评成功");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/seller/reviews";
  }
}
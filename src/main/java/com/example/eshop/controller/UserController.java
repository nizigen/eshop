package com.example.eshop.controller;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.*;
import com.example.eshop.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;
  private final OrderService orderService;
  private final PointsService pointsService;
  private final WalletService walletService;
  private final UserAddressService userAddressService;
  private final ReviewService reviewService;

  @Autowired
  public UserController(UserService userService, OrderService orderService, PointsService pointsService,
      WalletService walletService, UserAddressService userAddressService, ReviewService reviewService) {
    this.userService = userService;
    this.orderService = orderService;
    this.pointsService = pointsService;
    this.walletService = walletService;
    this.userAddressService = userAddressService;
    this.reviewService = reviewService;
  }

  // Utility method to get the current user
  private User getCurrentUser(Authentication authentication) {
    String username = authentication.getName();
    return userService.findByEmail(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
  }

  @GetMapping("/dashboard")
  public String userDashboard(Authentication authentication, Model model) {
    User user = getCurrentUser(authentication);

    // Get user's points
    Points points = pointsService.getPointsByUser(user);
    model.addAttribute("points", points.getBalance());

    // Get wallet balance
    Wallet wallet = walletService.getWalletByUser(user);
    model.addAttribute("walletBalance", wallet.getBalance());

    // Get order count
    long orderCount = orderService.countOrdersByUser(user);
    model.addAttribute("orderCount", orderCount);

    // Get recent orders (last 5)
    PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Order> recentOrders = orderService.findOrdersByUser(user, pageRequest);
    model.addAttribute("recentOrders", recentOrders.getContent());

    // Add user info
    model.addAttribute("user", user);

    return "user/dashboard";
  }

  @GetMapping("/orders")
  public String getOrders(Authentication authentication, Model model,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    User user = getCurrentUser(authentication);
    log.debug("Fetching orders for user: {}, page: {}, size: {}", user.getId(), page, size);

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Order> orders = orderService.findOrdersByUser(user, pageRequest);

    model.addAttribute("user", user);
    model.addAttribute("orders", orders);
    return "user/order-list";
  }

  @GetMapping("/profile")
  public String getProfile(Authentication authentication, Model model) {
    User user = getCurrentUser(authentication);
    UserProfile profile = userService.findUserProfile(user.getId())
        .orElseGet(() -> new UserProfile(user));
    Wallet wallet = walletService.getWalletByUser(user);
    Points points = pointsService.getPointsByUser(user);
    List<UserAddress> addresses = userAddressService.getCurrentUserAddresses();
    Page<Order> orders = orderService.findOrdersByUser(user,
        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));
    Page<PointsHistory> pointsHistory = pointsService.getPointsHistory(user.getId(),
        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));
    Page<Transaction> transactions = walletService.getTransactionHistory(user,
        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));

    // 新增：获取当前用户的评价
    List<Review> userReviews = reviewService.getUserReviews(user.getId());
    model.addAttribute("userReviews", userReviews);

    model.addAttribute("user", user);
    model.addAttribute("profile", profile);
    model.addAttribute("wallet", wallet);
    model.addAttribute("points", points);
    model.addAttribute("addresses", addresses);
    model.addAttribute("orders", orders);
    model.addAttribute("pointsHistory", pointsHistory);
    model.addAttribute("transactions", transactions);
    model.addAttribute("deductibleAmount", points != null ? points.getBalance() / 100.0 : 0.0);

    return "user/profile";
  }

  @GetMapping("/points")
  public String getPoints(Authentication authentication, Model model,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    User user = getCurrentUser(authentication);

    Points points = pointsService.getPointsByUser(user);
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<PointsHistory> pointsHistory = pointsService.getPointsHistory(user.getId(), pageRequest);

    model.addAttribute("user", user);
    model.addAttribute("points", points);
    model.addAttribute("pointsHistory", pointsHistory);
    model.addAttribute("deductibleAmount", points != null ? points.getBalance() / 100.0 : 0.0);
    return "user/points";
  }

  @GetMapping("/wallet")
  public String getWallet(Authentication authentication, Model model,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    User user = getCurrentUser(authentication);

    Wallet wallet = walletService.getWalletByUser(user);
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Transaction> transactions = walletService.getTransactionHistory(user, pageRequest);

    // 计算本月收支
    BigDecimal monthlyIncome = walletService.getMonthlyIncome(user);
    BigDecimal monthlyExpense = walletService.getMonthlyExpense(user);

    model.addAttribute("user", user);
    model.addAttribute("wallet", wallet);
    model.addAttribute("transactions", transactions);
    model.addAttribute("monthlyIncome", monthlyIncome);
    model.addAttribute("monthlyExpense", monthlyExpense);
    return "user/wallet";
  }
}
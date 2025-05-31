package com.example.eshop.controller;

import com.example.eshop.model.Seller;
import com.example.eshop.model.User;
import com.example.eshop.service.ProductService;
import com.example.eshop.service.OrderService;
import com.example.eshop.service.ReviewService;
import com.example.eshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.exception.UnauthorizedActionException;

@Controller
public class RoleController {

  @Autowired
  private UserService userService;

  @Autowired
  private ProductService productService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private ReviewService reviewService;

  @GetMapping({ "/seller", "/seller/dashboard" })
  @PreAuthorize("hasRole('ROLE_SELLER')")
  public String sellerPage(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.findByEmail(auth.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!(user instanceof Seller)) {
      throw new UnauthorizedActionException("Access denied: User is not a seller");
    }

    Seller seller = (Seller) user;
    model.addAttribute("seller", seller);

    // Add statistics
    model.addAttribute("productCount", productService.countProductsBySeller(seller));
    model.addAttribute("activeProductCount", productService.countActiveProductsBySeller(seller));
    model.addAttribute("pendingProductCount", productService.countPendingProductsBySeller(seller));

    model.addAttribute("orderCount", orderService.countOrdersBySeller(seller));
    model.addAttribute("pendingOrderCount", orderService.countPendingOrdersBySeller(seller));
    model.addAttribute("completedOrderCount", orderService.countCompletedOrdersBySeller(seller));

    // Add recent orders and reviews
    model.addAttribute("recentOrders", orderService.findRecentOrdersBySeller(seller, 5));
    model.addAttribute("recentReviews", reviewService.findRecentReviewsBySeller(seller.getId(), 5));

    return "seller/dashboard";
  }

  @GetMapping("/user")
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  public String userPage(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.findByEmail(auth.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    model.addAttribute("user", user);
    return "redirect:/user/profile";
  }
}
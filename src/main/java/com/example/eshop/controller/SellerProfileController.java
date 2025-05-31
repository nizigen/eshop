package com.example.eshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;
import java.util.Optional;

import com.example.eshop.model.User;
import com.example.eshop.model.Seller;
import com.example.eshop.service.UserService;
import com.example.eshop.service.ProductService;
import com.example.eshop.service.OrderService;
import com.example.eshop.service.ReviewService;
import com.example.eshop.model.UserProfile;
import com.example.eshop.repository.UserProfileRepository;
import com.example.eshop.exception.ResourceNotFoundException;

@Controller
@RequestMapping("/seller")
public class SellerProfileController {

  @Autowired
  private UserService userService;

  @Autowired
  private UserProfileRepository userProfileRepository;

  @Autowired
  private ProductService productService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private ReviewService reviewService;

  @GetMapping("/profile")
  public String getProfile(Model model, Principal principal) {
    if (principal == null) {
      return "redirect:/login";
    }

    Optional<User> userOpt = userService.findByEmail(principal.getName());
    if (!userOpt.isPresent()) {
      return "redirect:/login";
    }
    User user = userOpt.get();

    if (!(user instanceof Seller)) {
      return "redirect:/";
    }

    Seller seller = (Seller) user;
    UserProfile profile = userProfileRepository.findById(seller.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Profile not found for seller: " + seller.getId()));

    // Add seller and profile
    model.addAttribute("seller", seller);
    model.addAttribute("profile", profile);

    // Add statistics
    model.addAttribute("totalProducts", productService.countProductsBySeller(seller));
    model.addAttribute("activeProducts", productService.countActiveProductsBySeller(seller));
    model.addAttribute("soldProducts", productService.countSoldProductsBySeller(seller));

    model.addAttribute("totalOrders", orderService.countOrdersBySeller(seller));
    model.addAttribute("pendingOrders", orderService.countPendingOrdersBySeller(seller));
    model.addAttribute("completedOrders", orderService.countCompletedOrdersBySeller(seller));

    model.addAttribute("totalRevenue", orderService.calculateTotalRevenueBySeller(seller));
    model.addAttribute("monthlyRevenue", orderService.calculateMonthlyRevenueBySeller(seller));

    // Add recent reviews
    model.addAttribute("recentReviews", reviewService.findRecentReviewsBySeller(seller.getId(), 5));

    return "seller/profile";
  }

  @PostMapping("/profile/update")
  public String updateProfile(@ModelAttribute("seller") Seller seller,
      @ModelAttribute("profile") UserProfile profile,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (principal == null) {
      return "redirect:/login";
    }

    Optional<User> userOpt = userService.findByEmail(principal.getName());
    if (!userOpt.isPresent()) {
      return "redirect:/login";
    }
    User user = userOpt.get();

    if (!(user instanceof Seller)) {
      return "redirect:/";
    }

    Seller existingSeller = (Seller) user;
    UserProfile existingProfile = userProfileRepository.findById(existingSeller.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Profile not found for seller: " + existingSeller.getId()));

    // Update seller basic info
    existingSeller.setPhone(seller.getPhone());
    existingSeller.setEmail(seller.getEmail());
    existingSeller.setStoreDescription(seller.getStoreDescription());
    existingSeller.setAddress(seller.getAddress());

    // Update profile info
    existingProfile.setStoreName(profile.getStoreName());
    existingProfile.setBankAccount(profile.getBankAccount());

    userService.updateUser(existingSeller);
    userProfileRepository.save(existingProfile);

    redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
    return "redirect:/seller/profile";
  }
}
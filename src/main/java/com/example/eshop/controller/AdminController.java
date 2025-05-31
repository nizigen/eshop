package com.example.eshop.controller;

import com.example.eshop.model.User;
import com.example.eshop.model.UserStatus;
import com.example.eshop.model.Gender;
import com.example.eshop.model.Seller;
import com.example.eshop.service.UserService;
import com.example.eshop.service.ProductService;
import com.example.eshop.service.OrderService;
import com.example.eshop.service.SellerService;
import com.example.eshop.dto.SellerLevelUpdateDTO;
import com.example.eshop.dto.SellerStatusUpdateDTO;
import com.example.eshop.dto.ApiResponse;
import com.example.eshop.model.Product;
import com.example.eshop.model.ProductStatus;
import com.example.eshop.model.Order;
import com.example.eshop.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

  private static final Logger log = LoggerFactory.getLogger(AdminController.class);
  private final UserService userService;
  private final ProductService productService;
  private final OrderService orderService;
  private final SellerService sellerService;

  // --- Dashboard ---
  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    // 添加统计数据
    model.addAttribute("totalUsers", userService.countAllUsers());
    model.addAttribute("totalProducts", productService.countAllProducts());
    model.addAttribute("pendingUsers", userService.countUsersByStatus(UserStatus.PENDING));
    model.addAttribute("pendingProducts", productService.countProductsByStatus(ProductStatus.PENDING_APPROVAL));
    return "admin/dashboard";
  }

  // --- User Management ---

  // View all users
  @GetMapping("/users")
  public String viewAllUsers(HttpServletRequest request, Model model) {
    log.info("Admin request to view all users");
    model.addAttribute("currentUri", request.getRequestURI());
    List<User> users = userService.findAllUsers();
    model.addAttribute("users", users);
    model.addAttribute("pageTitle", "所有用户");
    return "admin/list-users";
  }

  // View pending users
  @GetMapping("/users/pending")
  public String viewPendingUsers(HttpServletRequest request, Model model) {
    log.info("Admin request to view pending users");
    model.addAttribute("currentUri", request.getRequestURI());
    List<User> pendingUsers = userService.findUsersByStatus(UserStatus.PENDING);
    model.addAttribute("pendingUsers", pendingUsers);
    model.addAttribute("pageTitle", "待审核用户");
    return "admin/pending-users"; // Thymeleaf template name
  }

  // Approve user registration
  @PostMapping("/users/approve/{userId}")
  public String approveUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
    log.info("Admin request to approve user ID: {}", userId);
    try {
      userService.approveUser(userId);
      redirectAttributes.addFlashAttribute("successMessage", "用户 ID " + userId + " 已成功批准！");
    } catch (Exception e) {
      log.error("Error approving user ID {}: {}", userId, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", "批准用户 ID " + userId + " 时出错: " + e.getMessage());
    }
    // Redirect back to the pending users page
    return "redirect:/admin/users/pending";
  }

  // Reject user registration
  @PostMapping("/users/reject/{userId}")
  public String rejectUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
    log.info("Admin request to reject user ID: {}", userId);
    try {
      userService.rejectUser(userId);
      redirectAttributes.addFlashAttribute("successMessage", "用户 ID " + userId + " 已成功拒绝！");
    } catch (Exception e) {
      log.error("Error rejecting user ID {}: {}", userId, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", "拒绝用户 ID " + userId + " 时出错: " + e.getMessage());
    }
    // Redirect back to the pending users page
    return "redirect:/admin/users/pending";
  }

  // --- Product Management (Moved to AdminProductController) ---
  /*
   * @GetMapping("/products")
   * public String viewAllProducts(Model model) {
   * log.info("Admin request to view all products");
   * List<Product> products = productService.findAllProducts();
   * model.addAttribute("products", products);
   * model.addAttribute("pageTitle", "所有商品");
   * return "admin/products"; // Thymeleaf template name
   * }
   * 
   * @GetMapping("/products/pending")
   * public String viewPendingProducts(Model model) {
   * log.info("Admin request to view pending products");
   * List<Product> pendingProducts =
   * productService.findProductsByStatus(ProductStatus.PENDING_APPROVAL);
   * model.addAttribute("products", pendingProducts);
   * model.addAttribute("pageTitle", "待审核商品");
   * return "admin/products"; // Can reuse the same template, filtering/display
   * logic is inside
   * }
   * 
   * @PostMapping("/products/approve/{productId}")
   * public String approveProduct(@PathVariable Long productId, RedirectAttributes
   * redirectAttributes) {
   * log.info("Admin request to approve product ID: {}", productId);
   * try {
   * productService.approveProduct(productId);
   * redirectAttributes.addFlashAttribute("successMessage", "商品 ID " + productId +
   * " 已成功批准！");
   * } catch (Exception e) {
   * log.error("Error approving product ID {}: {}", productId, e.getMessage());
   * redirectAttributes.addFlashAttribute("errorMessage", "批准商品 ID " + productId +
   * " 时出错: " + e.getMessage());
   * }
   * return "redirect:/admin/products/pending";
   * }
   * 
   * @PostMapping("/products/reject/{productId}")
   * public String rejectProduct(@PathVariable Long productId, RedirectAttributes
   * redirectAttributes) {
   * log.info("Admin request to reject product ID: {}", productId);
   * try {
   * productService.rejectProduct(productId);
   * redirectAttributes.addFlashAttribute("successMessage", "商品 ID " + productId +
   * " 已成功拒绝！");
   * } catch (Exception e) {
   * log.error("Error rejecting product ID {}: {}", productId, e.getMessage());
   * redirectAttributes.addFlashAttribute("errorMessage", "拒绝商品 ID " + productId +
   * " 时出错: " + e.getMessage());
   * }
   * return "redirect:/admin/products/pending";
   * }
   */

  // --- Order Management ---
  @GetMapping("/orders")
  public String viewAllOrders(HttpServletRequest request, Model model) {
    log.info("Admin request to view all orders");
    model.addAttribute("currentUri", request.getRequestURI());
    List<Order> orders = orderService.findAllOrders();
    model.addAttribute("orders", orders);
    model.addAttribute("pageTitle", "所有订单");
    return "admin/orders"; // Thymeleaf template name
  }

  @GetMapping("/orders/{id}")
  public String viewOrderDetail(@PathVariable Long id, Model model) {
    log.info("Admin request to view order detail: {}", id);
    Order order = orderService.findOrderById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    model.addAttribute("order", order);
    model.addAttribute("pageTitle", "订单详情");
    return "admin/order/detail";
  }

  // --- User Management ---

  @PostMapping("/users/{userId}/delete")
  public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
    log.info("Admin request: Deleting user with ID: {}", userId);
    try {
      userService.deleteUser(userId);
      redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
    } catch (Exception e) {
      log.error("Error deleting user {}: {}", userId, e.getMessage(), e);
      redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
    }
    return "redirect:/admin/users"; // Redirect back to the user list
  }

  // --- Edit User ---

  @GetMapping("/users/{userId}/edit")
  public String showEditUserForm(@PathVariable Long userId, HttpServletRequest request, Model model) {
    log.info("Admin request: Showing edit form for user ID: {}", userId);
    try {
      User user = userService.findUserById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + userId)); // Handle Optional
      model.addAttribute("user", user); // Add user to model for the form
      model.addAttribute("allStatuses", UserStatus.values()); // Add statuses for dropdown
      model.addAttribute("allGenders", Gender.values()); // Add genders for dropdown
      // Do NOT add roles easily - changing role has significant implications
      return "admin/edit-user"; // Path to the edit form template
    } catch (Exception e) {
      log.error("Error fetching user {} for edit: {}", userId, e.getMessage(), e);
      model.addAttribute("errorMessage", "Could not load user for editing: " + e.getMessage());
      // Redirect back to the list or show an error page?
      return "redirect:/admin/users"; // Redirecting back to list for simplicity
    }
  }

  @PostMapping("/users/{userId}/edit")
  public String processEditUserForm(@PathVariable Long userId,
      @Valid @ModelAttribute("user") User user, // Bind form data to User object
      BindingResult result, // Capture validation results
      RedirectAttributes redirectAttributes,
      Model model) {
    log.info("Admin request: Processing edit for user ID: {}", userId);

    // Ensure the ID from the path matches the one in the model attribute
    if (!userId.equals(user.getId())) {
      log.error("Path variable ID {} does not match user object ID {} during edit", userId, user.getId());
      redirectAttributes.addFlashAttribute("errorMessage", "Error processing user update: ID mismatch.");
      return "redirect:/admin/users";
    }

    // Basic validation (more specific validation might be needed)
    if (result.hasErrors()) {
      log.warn("Validation errors editing user {}: {}", userId, result.getAllErrors());
      model.addAttribute("allStatuses", UserStatus.values()); // Re-add for dropdowns if returning to form
      model.addAttribute("allGenders", Gender.values());
      return "admin/edit-user"; // Return to edit form with errors
    }

    try {
      // Call service to update (Service layer handles fetching existing and updating)
      userService.updateUser(user);
      redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
      return "redirect:/admin/users"; // Redirect to user list after successful update
    } catch (Exception e) {
      log.error("Error updating user {}: {}", userId, e.getMessage(), e);
      // Add error message to model for display on the edit form
      model.addAttribute("errorMessage", "Error updating user: " + e.getMessage());
      model.addAttribute("allStatuses", UserStatus.values()); // Re-add for dropdowns
      model.addAttribute("allGenders", Gender.values());
      // Return to the edit form on error
      return "admin/edit-user";
    }
  }

  @PostMapping("/sellers/{sellerId}/status")
  public ResponseEntity<?> updateSellerStatus(@PathVariable Long sellerId,
      @RequestBody SellerStatusUpdateDTO statusDTO) {
    sellerService.updateSellerStatus(sellerId, statusDTO.getStatus());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/sellers/{sellerId}/level")
  public ResponseEntity<?> updateSellerLevel(@PathVariable Long sellerId, @RequestBody SellerLevelUpdateDTO levelDTO) {
    try {
      sellerService.updateSellerLevel(sellerId, levelDTO.getLevel());
      return ResponseEntity.ok(ApiResponse.success("商家等级更新成功"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @GetMapping("/sellers")
  public String viewSellers(Model model) {
    List<Seller> sellers = sellerService.findAllSellers();
    model.addAttribute("sellers", sellers);
    return "admin/sellers";
  }

  @GetMapping("/sellers/{sellerId}/details")
  public String viewSellerDetails(@PathVariable Long sellerId, Model model) {
    Seller seller = sellerService.findById(sellerId)
        .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));
    model.addAttribute("seller", seller);
    return "admin/seller-details";
  }

  @GetMapping("/seller-levels")
  public String viewSellerLevels(Model model) {
    // model.addAttribute("levels", ...);
    return "admin/seller-levels";
  }
}
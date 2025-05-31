package com.example.eshop.controller.seller;

import com.example.eshop.dto.ApiResponse;
import com.example.eshop.model.Order;
import com.example.eshop.model.OrderStatus;
import com.example.eshop.model.Seller;
import com.example.eshop.model.ReturnRequest;
import com.example.eshop.service.OrderService;
import com.example.eshop.service.UserService;
import com.example.eshop.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/seller/orders")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class SellerOrderController {

  private static final Logger log = LoggerFactory.getLogger(SellerOrderController.class);
  private final OrderService orderService;
  private final UserService userService;
  private final ReturnRequestRepository returnRequestRepository;

  @GetMapping
  public String listOrders(@RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    Seller seller = userService.findSellerByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Seller not found"));

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Order> orders = orderService.findOrdersBySeller(seller, status, pageRequest);
    long returnRequestCount = orderService.countPendingReturnRequests(seller);

    model.addAttribute("orders", orders);
    model.addAttribute("status", status);
    model.addAttribute("statuses", OrderStatus.values());
    model.addAttribute("returnRequestCount", returnRequestCount);
    return "seller/orders";
  }

  @GetMapping("/pending")
  public String listPendingOrders(@AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    Seller seller = userService.findSellerByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Seller not found"));

    List<Order> pendingOrders = orderService.findBySellerAndStatus(seller, OrderStatus.PENDING_DELIVERY);
    model.addAttribute("orders", pendingOrders);
    return "seller/pending-orders";
  }

  @PostMapping("/{orderId}/ship")
  @ResponseBody
  public ApiResponse<Void> shipOrder(@PathVariable Long orderId,
      @AuthenticationPrincipal UserDetails userDetails) {
    Seller seller = userService.findSellerByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Seller not found"));

    Order order = orderService.findOrderById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    // 验证订单是否属于该卖家
    boolean hasAccess = order.getOrderItems().stream()
        .anyMatch(item -> item.getSeller().getId().equals(seller.getId()));
    if (!hasAccess) {
      return ApiResponse.error("Unauthorized access to order");
    }

    if (order.getStatus() != OrderStatus.PENDING_DELIVERY) {
      return ApiResponse.error("Order cannot be shipped in current status");
    }

    try {
      orderService.updateOrderStatus(orderId, OrderStatus.IN_TRANSIT);
      return ApiResponse.success("Order shipped successfully");
    } catch (IllegalStateException e) {
      return ApiResponse.error(e.getMessage());
    }
  }

  @GetMapping("/pending-returns")
  public String listPendingReturns(@AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    Seller seller = userService.findSellerByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Seller not found"));

    List<ReturnRequest> returnRequests = returnRequestRepository
        .findByOrderItemsSellerId(seller.getId(), PageRequest.of(0, 100)).getContent();
    model.addAttribute("returnRequests", returnRequests);
    return "seller/pending-returns";
  }

  @PostMapping("/returns/{requestId}/process")
  @ResponseBody
  public ApiResponse<Void> processReturnRequest(@PathVariable Long requestId,
      @RequestParam boolean approved,
      @AuthenticationPrincipal UserDetails userDetails) {
    return orderService.processReturnRequest(requestId, approved, userDetails.getUsername());
  }

  @GetMapping("/{id}")
  public String getOrderDetail(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    Seller seller = userService.findSellerByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Seller not found"));

    Order order = orderService.findOrderById(id)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    // 验证订单是否属于该卖家
    boolean hasAccess = order.getOrderItems().stream()
        .anyMatch(item -> item.getSeller().getId().equals(seller.getId()));
    if (!hasAccess) {
      throw new RuntimeException("Unauthorized access to order");
    }

    model.addAttribute("order", order);
    model.addAttribute("seller", seller);
    return "seller/order-detail";
  }

  @PostMapping("/{id}/deliver")
  @ResponseBody
  public ApiResponse<Void> deliverOrder(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
    try {
      return orderService.deliverOrder(id, userDetails.getUsername());
    } catch (Exception e) {
      log.error("Error delivering order {}: {}", id, e.getMessage(), e);
      return ApiResponse.error(e.getMessage());
    }
  }
}
package com.example.eshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.eshop.service.OrderService;
import com.example.eshop.service.UserService;
import com.example.eshop.service.UserAddressService;
import com.example.eshop.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.example.eshop.dto.OrderDTO;
import com.example.eshop.dto.OrderCreateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import com.example.eshop.dto.ReturnRequestDTO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.eshop.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import com.example.eshop.model.Order;
import com.example.eshop.model.OrderItem;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.eshop.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping({ "/orders", "/api/orders", "/order" })
public class OrderController {

  private final OrderService orderService;
  private final UserService userService;
  private final UserAddressService userAddressService;
  private static final Logger log = LoggerFactory.getLogger(OrderController.class);

  @GetMapping("/create")
  public String showCreateOrderPage(@AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    String email = userDetails.getUsername();
    model.addAttribute("addresses", userAddressService.getCurrentUserAddresses());
    model.addAttribute("walletBalance", userService.getWalletBalance(email));
    model.addAttribute("availablePoints", userService.getAvailablePoints(email));
    model.addAttribute("totalAmount", orderService.calculateTotalAmount(email));

    // 转换为DTO
    java.util.List<com.example.eshop.dto.UserCouponDTO> couponDTOs = userService.getUserCoupons(email).stream()
        .map(uc -> {
          com.example.eshop.dto.UserCouponDTO dto = new com.example.eshop.dto.UserCouponDTO();
          dto.setId(uc.getId());
          dto.setStatus(uc.getStatus().name());
          dto.setCouponId(uc.getCoupon().getId());
          dto.setName(uc.getCoupon().getName());
          dto.setType(uc.getCoupon().getType().name());
          dto.setValue(uc.getCoupon().getValue().doubleValue());
          dto.setMinPurchase(uc.getCoupon().getMinPurchase().doubleValue());
          dto.setStartTime(uc.getCoupon().getStartTime().toString());
          dto.setEndTime(uc.getCoupon().getEndTime().toString());
          return dto;
        }).collect(java.util.stream.Collectors.toList());

    model.addAttribute("userCoupons", couponDTOs);
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      model.addAttribute("userCouponsJson", mapper.writeValueAsString(couponDTOs));
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      log.error("Error converting user coupons to JSON", e);
      model.addAttribute("userCouponsJson", "[]");
    }
    return "order/checkout";
  }

  @PostMapping("/create")
  @ResponseBody
  public ApiResponse<OrderDTO> createOrder(@Valid @RequestBody OrderCreateRequest request,
      Authentication authentication) {
    return orderService.createOrder(request, authentication.getName(), request.getUserCouponId());
  }

  @GetMapping("/{orderId}/items/{itemId}/return")
  public String showReturnRequestForm(@PathVariable Long orderId,
      @PathVariable Long itemId,
      Model model,
      @AuthenticationPrincipal UserDetails userDetails) {
    String email = userDetails.getUsername();
    Order order = orderService.findOrderById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    // 验证订单属于当前用户
    if (!order.getUser().getEmail().equals(email)) {
      throw new AccessDeniedException("Not authorized to access this order");
    }

    // 获取订单项
    OrderItem orderItem = order.getOrderItems().stream()
        .filter(item -> item.getId().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

    // 检查是否可以申请退货
    boolean isReturnExpired = false;
    if (order.getDeliveryTime() != null) {
      LocalDateTime returnDeadline = order.getDeliveryTime().plusHours(24);
      isReturnExpired = LocalDateTime.now().isAfter(returnDeadline);
    }

    model.addAttribute("order", order);
    model.addAttribute("orderItem", orderItem);
    model.addAttribute("isReturnExpired", isReturnExpired);

    return "order/return-request";
  }

  @PostMapping("/{orderId}/items/{itemId}/return")
  public String submitReturnRequest(@PathVariable Long orderId,
      @PathVariable Long itemId,
      @ModelAttribute ReturnRequestDTO returnRequest,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes) {
    String email = userDetails.getUsername();
    try {
      orderService.submitReturnRequest(orderId, itemId, returnRequest, email);
      redirectAttributes.addFlashAttribute("successMessage", "退货申请已提交，请等待商家审核");
    } catch (IllegalStateException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/orders/" + orderId;
  }

  @PostMapping("/{orderId}/return-request")
  @ResponseBody
  public ApiResponse<Void> submitReturnRequestApi(@PathVariable Long orderId,
      @RequestBody ReturnRequestDTO returnRequest,
      @AuthenticationPrincipal UserDetails userDetails) {
    String email = userDetails.getUsername();
    try {
      // 获取订单项ID（这里假设是第一个订单项，实际应该由前端传入）
      Order order = orderService.findOrderById(orderId)
          .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
      Long itemId = order.getOrderItems().get(0).getId();

      return orderService.submitReturnRequest(orderId, itemId, returnRequest, email);
    } catch (Exception e) {
      log.error("Error submitting return request for order {}: {}", orderId, e.getMessage(), e);
      return ApiResponse.error(e.getMessage());
    }
  }

  @GetMapping
  public String listOrders(@RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    String email = userDetails.getUsername();
    model.addAttribute("orders", orderService.getUserOrders(email, status, page));
    model.addAttribute("status", status);
    return "order/list";
  }

  @GetMapping("/{id}")
  public String getOrderDetail(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    String email = userDetails.getUsername();
    model.addAttribute("order", orderService.getOrderDetail(id, email));
    return "user/order-detail";
  }

  @PostMapping("/{id}/confirm")
  @ResponseBody
  public ApiResponse<Void> confirmReceipt(@PathVariable Long id, Authentication authentication) {
    return orderService.confirmReceipt(id, authentication.getName());
  }

  @PostMapping("/{id}/confirm-receipt")
  @ResponseBody
  public ApiResponse<Void> confirmReceiptApi(@PathVariable Long id, Authentication authentication) {
    return orderService.confirmReceipt(id, authentication.getName());
  }

  @PostMapping("/{id}/pay")
  @ResponseBody
  public ApiResponse<Map<String, String>> payOrder(@PathVariable Long id, Authentication authentication) {
    try {
      String email = authentication.getName();
      Order order = orderService.findOrderById(id)
          .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

      // 验证订单所有权
      if (!order.getUser().getEmail().equals(email)) {
        throw new AccessDeniedException("You don't have permission to pay this order");
      }

      // 验证订单状态
      if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
        return ApiResponse.error("Order is not in pending payment status");
      }

      // 调用支付服务
      String payUrl = orderService.createPayment(order);

      Map<String, String> response = new HashMap<>();
      response.put("payUrl", payUrl);
      return ApiResponse.success("Payment URL generated", response);
    } catch (Exception e) {
      log.error("Error processing payment for order {}: {}", id, e.getMessage(), e);
      return ApiResponse.error(e.getMessage());
    }
  }

  @PostMapping("/{id}/cancel")
  @ResponseBody
  public ApiResponse<Void> cancelOrder(@PathVariable Long id, Authentication authentication) {
    try {
      String email = authentication.getName();
      Order order = orderService.findOrderById(id)
          .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

      // 验证订单所有权
      if (!order.getUser().getEmail().equals(email)) {
        throw new AccessDeniedException("You don't have permission to cancel this order");
      }

      // 验证订单状态
      if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
        return ApiResponse.error("Only pending payment orders can be cancelled");
      }

      orderService.cancelOrder(id, email);
      return ApiResponse.success("Order cancelled successfully", null);
    } catch (Exception e) {
      log.error("Error cancelling order {}: {}", id, e.getMessage(), e);
      return ApiResponse.error(e.getMessage());
    }
  }

  @GetMapping("/checkout")
  public String showCheckoutPage(@RequestParam Long productId,
      @RequestParam(defaultValue = "1") Integer quantity,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    String email = userDetails.getUsername();
    model.addAttribute("addresses", userAddressService.getCurrentUserAddresses());
    model.addAttribute("walletBalance", userService.getWalletBalance(email));
    model.addAttribute("availablePoints", userService.getAvailablePoints(email));
    model.addAttribute("productId", productId);
    model.addAttribute("quantity", quantity);

    // 计算商品总价
    BigDecimal totalAmount = orderService.calculateProductTotal(productId, quantity);
    model.addAttribute("totalAmount", totalAmount);

    // 转换为DTO
    java.util.List<com.example.eshop.dto.UserCouponDTO> couponDTOs = userService.getUserCoupons(email).stream()
        .map(uc -> {
          com.example.eshop.dto.UserCouponDTO dto = new com.example.eshop.dto.UserCouponDTO();
          dto.setId(uc.getId());
          dto.setStatus(uc.getStatus().name());
          dto.setCouponId(uc.getCoupon().getId());
          dto.setName(uc.getCoupon().getName());
          dto.setType(uc.getCoupon().getType().name());
          dto.setValue(uc.getCoupon().getValue().doubleValue());
          dto.setMinPurchase(uc.getCoupon().getMinPurchase().doubleValue());
          dto.setStartTime(uc.getCoupon().getStartTime().toString());
          dto.setEndTime(uc.getCoupon().getEndTime().toString());
          return dto;
        }).collect(java.util.stream.Collectors.toList());

    model.addAttribute("userCoupons", couponDTOs);
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      model.addAttribute("userCouponsJson", mapper.writeValueAsString(couponDTOs));
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      log.error("Error converting user coupons to JSON", e);
      model.addAttribute("userCouponsJson", "[]");
    }
    return "order/checkout";
  }
}
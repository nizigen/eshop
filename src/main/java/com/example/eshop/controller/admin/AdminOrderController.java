package com.example.eshop.controller.admin;

import com.example.eshop.dto.ApiResponse;
import com.example.eshop.model.Order;
import com.example.eshop.model.OrderStatus;
import com.example.eshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/order-management")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

  private final OrderService orderService;

  @GetMapping
  public String listOrders(@RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      Model model) {
    List<Order> orders = orderService.findAllOrders();
    model.addAttribute("orders", orders);
    model.addAttribute("statuses", OrderStatus.values());
    return "admin/order/list";
  }

  @PostMapping("/{id}/status")
  @ResponseBody
  public ApiResponse<Void> updateOrderStatus(@PathVariable Long id,
      @RequestParam OrderStatus newStatus) {
    try {
      orderService.updateOrderStatus(id, newStatus);
      return ApiResponse.success("订单状态更新成功");
    } catch (Exception e) {
      return ApiResponse.error("订单状态更新失败：" + e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public String getOrderDetail(@PathVariable Long id, Model model) {
    Order order = orderService.findOrderById(id)
        .orElseThrow(() -> new RuntimeException("订单不存在"));
    model.addAttribute("order", order);
    model.addAttribute("statuses", OrderStatus.values());
    return "admin/order/detail";
  }
}
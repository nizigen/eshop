package com.example.eshop.service;

import com.example.eshop.dto.ApiResponse;
import com.example.eshop.dto.OrderDTO;
import com.example.eshop.dto.OrderCreateRequest;
import com.example.eshop.dto.ReturnRequestDTO;
import com.example.eshop.model.Order;
import com.example.eshop.model.OrderStatus;
import com.example.eshop.model.Product;
import com.example.eshop.model.Seller;
import com.example.eshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {
  // User order operations
  Page<OrderDTO> getUserOrders(String email, String status, int page);

  OrderDTO getOrderDetail(Long id, String email);

  double calculateTotalAmount(String email);

  ApiResponse<OrderDTO> createOrder(OrderCreateRequest request, String email, Long userCouponId);

  ApiResponse<Void> confirmReceipt(Long id, String email);

  ApiResponse<Void> cancelOrder(Long id, String email);

  long countOrdersByUser(User user);

  Page<Order> findOrdersByUser(User user, Pageable pageable);

  // Admin operations
  List<Order> findAllOrders();

  Optional<Order> findOrderById(Long orderId);

  void updateOrderStatus(Long orderId, OrderStatus newStatus);

  void deleteOrder(Long orderId);

  // Seller operations
  long countOrdersBySeller(Seller seller);

  long countPendingOrdersBySeller(Seller seller);

  long countCompletedOrdersBySeller(Seller seller);

  long countPendingReturnRequests(Seller seller);

  Page<Order> findRecentOrdersBySeller(Seller seller, int limit);

  BigDecimal calculateTotalRevenueBySeller(Seller seller);

  BigDecimal calculateMonthlyRevenueBySeller(Seller seller);

  List<Order> findPendingOrdersBySeller(Seller seller);

  List<Order> findBySellerAndStatus(Seller seller, OrderStatus status);

  List<Order> findBySellerAndStatusAndCreatedAtAfter(Seller seller, OrderStatus status, LocalDateTime date);

  // Add new method for paginated seller orders
  Page<Order> findOrdersBySeller(Seller seller, String status, Pageable pageable);

  // Order processing
  void awardPointsForOrder(Order order);

  void settleSellerFunds(Long orderId);

  Order createOrderFromCart(User user, int pointsToUse, Long userCouponId, boolean isOfflineTransaction,
      String offlineLocation,
      String offlineTime);

  // Additional methods
  void payOrder(Order order);

  void confirmReceipt(Long orderId);

  List<Order> findInProgressOrdersByUser(User user);

  List<Order> findPendingSettlementOrdersBySeller(Seller seller);

  /**
   * 提交退货申请
   * 
   * @param orderId       订单ID
   * @param itemId        订单项ID
   * @param returnRequest 退货申请信息
   * @param email         用户邮箱
   * @return 处理结果
   */
  ApiResponse<Void> submitReturnRequest(Long orderId, Long itemId, ReturnRequestDTO returnRequest, String email);

  /**
   * 卖家处理退货申请
   * 
   * @param requestId 退货申请ID
   * @param approved  是否批准
   * @param email     卖家邮箱
   * @return 处理结果
   */
  ApiResponse<Void> processReturnRequest(Long requestId, boolean approved, String email);

  /**
   * 创建订单支付
   * 
   * @param order 订单
   * @return 支付URL
   */
  String createPayment(Order order);

  /**
   * 确认订单送达
   * 
   * @param orderId 订单ID
   * @param email   卖家邮箱
   * @return 处理结果
   */
  ApiResponse<Void> deliverOrder(Long orderId, String email);

  public BigDecimal calculateProductTotal(Long productId, Integer quantity);
}
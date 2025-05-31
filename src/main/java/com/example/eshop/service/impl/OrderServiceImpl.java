package com.example.eshop.service.impl;

import com.example.eshop.dto.ApiResponse;
import com.example.eshop.dto.OrderDTO;
import com.example.eshop.dto.OrderItemDTO;
import com.example.eshop.dto.OrderCreateRequest;
import com.example.eshop.dto.ReturnRequestDTO;
import com.example.eshop.exception.InsufficientBalanceException;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.*;
import com.example.eshop.repository.*;
import com.example.eshop.service.OrderService;
import com.example.eshop.service.PointsService;
import com.example.eshop.service.UserService;
import com.example.eshop.service.WalletService;
import com.example.eshop.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final CartItemRepository cartItemRepository;
  private final PointsService pointsService;
  private final WalletService walletService;
  private final PendingFundRepository pendingFundRepository;
  private final TransactionRepository transactionRepository;
  private final UserService userService;
  private final SellerFeeRateRepository sellerFeeRateRepository;
  private final ReturnRequestRepository returnRequestRepository;
  private final ReturnRequestItemRepository returnRequestItemRepository;
  private final CouponService couponService;
  private final WalletRepository walletRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Order> findAllOrders() {
    log.info("Fetching all orders");
    return orderRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Order> findOrderById(Long orderId) {
    log.info("Fetching order by ID: {}", orderId);
    return orderRepository.findById(orderId);
  }

  @Override
  @Transactional
  public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
    log.info("Attempting to update status for order {} to {}", orderId, newStatus);
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

    // 验证状态转换的合法性
    validateStatusTransition(order.getStatus(), newStatus);

    // 更新订单状态
    order.setStatus(newStatus);

    // 根据新状态更新相关时间戳
    updateOrderTimestamps(order, newStatus);

    // 处理特定状态的业务逻辑
    handleStatusSpecificLogic(order, newStatus);

    // 保存订单
    orderRepository.save(order);
    log.info("Successfully updated status for order {} to {}", orderId, newStatus);
  }

  private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
    // 定义合法的状态转换
    boolean isValidTransition = switch (currentStatus) {
      case PENDING_PAYMENT -> newStatus == OrderStatus.PENDING_DELIVERY || newStatus == OrderStatus.CANCELED;
      case PENDING_DELIVERY -> newStatus == OrderStatus.IN_TRANSIT || newStatus == OrderStatus.CANCELED;
      case IN_TRANSIT -> newStatus == OrderStatus.DELIVERED;
      case DELIVERED -> newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.RETURN_REQUESTED;
      case RETURN_REQUESTED -> newStatus == OrderStatus.RETURN_APPROVED || newStatus == OrderStatus.RETURN_REJECTED;
      default -> false;
    };

    if (!isValidTransition) {
      throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
    }
  }

  private void updateOrderTimestamps(Order order, OrderStatus newStatus) {
    LocalDateTime now = LocalDateTime.now();
    switch (newStatus) {
      case PENDING_DELIVERY -> order.setPaymentTime(now);
      case DELIVERED -> order.setDeliveryTime(now);
      case COMPLETED, AUTO_COMPLETED -> order.setCompletionTime(now);
      case RETURN_REQUESTED -> order.setReturnRequestTime(now);
      default -> {
      }
    }
  }

  private void handleStatusSpecificLogic(Order order, OrderStatus newStatus) {
    switch (newStatus) {
      case COMPLETED, AUTO_COMPLETED -> {
        // 结算卖家资金
        settleSellerFunds(order.getId());
        // 奖励积分
        awardPointsForOrder(order);
        // 更新商品库存和销量
        for (OrderItem item : order.getOrderItems()) {
          Product product = item.getProduct();
          product.setQuantity(product.getQuantity() - item.getQuantity());
          product.setSalesCount(product.getSalesCount() + item.getQuantity());
          productRepository.save(product);
        }
      }
      case RETURN_APPROVED -> {
        // 处理退款逻辑
        handleRefund(order);
      }
      default -> {
      }
    }
  }

  private void handleRefund(Order order) {
    // 退款到用户钱包
    try {
      // 获取用户钱包
      Wallet userWallet = walletService.getWalletByUser(order.getUser());

      // 更新用户钱包余额
      userWallet.setBalance(userWallet.getBalance().add(order.getTotalAmount()));
      walletRepository.save(userWallet);

      // 记录退款交易
      Transaction transaction = new Transaction();
      transaction.setUser(order.getUser());
      transaction.setType(TransactionType.REFUND_IN);
      transaction.setAmount(order.getTotalAmount());
      transaction.setBalanceAfter(userWallet.getBalance());
      transaction.setDescription("订单退款: " + order.getOrderNumber());
      transaction.setRelatedOrder(order);
      transactionRepository.save(transaction);

      // 从卖家钱包扣除退款金额
      for (OrderItem item : order.getOrderItems()) {
        Seller seller = item.getSeller();
        BigDecimal itemAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        // 获取卖家钱包
        Wallet sellerWallet = walletService.getWalletByUser(seller);

        // 更新卖家钱包余额
        sellerWallet.setBalance(sellerWallet.getBalance().subtract(itemAmount));
        walletRepository.save(sellerWallet);

        // 记录卖家退款交易
        Transaction sellerTransaction = new Transaction();
        sellerTransaction.setUser(seller);
        sellerTransaction.setType(TransactionType.REFUND_OUT);
        sellerTransaction.setAmount(itemAmount.negate());
        sellerTransaction.setBalanceAfter(sellerWallet.getBalance());
        sellerTransaction.setDescription("订单退款: " + order.getOrderNumber());
        sellerTransaction.setRelatedOrder(order);
        transactionRepository.save(sellerTransaction);
      }

      // 如果使用了积分，返还积分
      if (order.getPointsDeducted() > 0) {
        pointsService.addPoints(order.getUser().getId(), order.getPointsDeducted(),
            String.format("订单退款返还积分 (订单号: %s)", order.getOrderNumber()));
      }
    } catch (Exception e) {
      log.error("Error processing refund for order {}: {}", order.getOrderNumber(), e.getMessage(), e);
      throw new RuntimeException("退款处理失败", e);
    }
  }

  @Override
  @Transactional
  public void deleteOrder(Long orderId) {
    orderRepository.deleteById(orderId);
  }

  @Override
  @Transactional(readOnly = true)
  public long countOrdersBySeller(Seller seller) {
    return orderRepository.countBySeller(seller);
  }

  @Override
  @Transactional(readOnly = true)
  public long countPendingOrdersBySeller(Seller seller) {
    return orderRepository.countBySellerAndStatus(seller, OrderStatus.PENDING_PAYMENT);
  }

  @Override
  @Transactional(readOnly = true)
  public long countCompletedOrdersBySeller(Seller seller) {
    return orderRepository.countBySellerAndStatus(seller, OrderStatus.COMPLETED);
  }

  @Override
  @Transactional(readOnly = true)
  public long countPendingReturnRequests(Seller seller) {
    return orderRepository.countBySellerAndStatus(seller, OrderStatus.RETURN_REQUESTED);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> findRecentOrdersBySeller(Seller seller, int limit) {
    return orderRepository.findBySellerOrderByCreatedAtDesc(
        seller,
        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")));
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal calculateTotalRevenueBySeller(Seller seller) {
    return orderRepository.findBySellerAndStatus(seller, OrderStatus.COMPLETED)
        .stream()
        .map(Order::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal calculateMonthlyRevenueBySeller(Seller seller) {
    LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
    return orderRepository.findBySellerAndStatusAndCreatedAtAfter(seller, OrderStatus.COMPLETED, monthAgo)
        .stream()
        .map(Order::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> findPendingOrdersBySeller(Seller seller) {
    log.info("Fetching pending orders for seller: {}", seller.getId());
    return orderRepository.findBySellerAndStatus(seller, OrderStatus.PENDING_PAYMENT);
  }

  @Override
  @Transactional
  public void payOrder(Order order) {
    // 检查订单状态
    if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
      throw new IllegalStateException("订单状态不正确");
    }

    // 扣除用户钱包余额（只扣钱，不写交易记录）
    Wallet userWallet = walletService.getWalletByUser(order.getUser());
    if (userWallet.getBalance().compareTo(order.getTotalAmount()) < 0) {
      throw new InsufficientBalanceException("余额不足，请先充值");
    }
    userWallet.setBalance(userWallet.getBalance().subtract(order.getTotalAmount()));
    walletService.getWalletByUser(order.getUser()); // 保证钱包已更新

    // 记录支出交易（只写一条PURCHASE记录，金额为负数）
    Transaction transaction = new Transaction();
    transaction.setUser(order.getUser());
    transaction.setType(TransactionType.PURCHASE);
    transaction.setAmount(order.getTotalAmount().negate()); // 负数
    transaction.setBalanceAfter(userWallet.getBalance());
    transaction.setDescription("订单支付: " + order.getOrderNumber());
    transaction.setRelatedOrder(order);
    transactionRepository.save(transaction);

    // 更新订单状态
    order.setStatus(OrderStatus.PENDING_DELIVERY);
    order.setPaymentTime(LocalDateTime.now());
    orderRepository.save(order);

    // 创建待结算资金记录
    for (OrderItem item : order.getOrderItems()) {
      Seller seller = item.getSeller();
      // 获取商家等级对应的费率
      BigDecimal feeRate = sellerFeeRateRepository.findByLevel(seller.getSellerDetails().getSellerLevel())
          .map(SellerFeeRate::getFeeRate)
          .orElse(BigDecimal.valueOf(0.0100)); // 默认1%费率（等级5）

      PendingFund pendingFund = new PendingFund();
      pendingFund.setOrderItem(item);
      pendingFund.setSeller(seller);
      pendingFund.setAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
      pendingFund.setFeeRate(feeRate);
      pendingFund.setPlatformFee(pendingFund.getAmount().multiply(feeRate));
      pendingFund.setStatus(PendingFundStatus.HELD);
      pendingFund.setReleaseDueDate(LocalDateTime.now().plusDays(7)); // 7天后自动结算
      pendingFundRepository.save(pendingFund);
    }

    // 更新商品库存和销量
    for (OrderItem item : order.getOrderItems()) {
      Product product = item.getProduct();
      product.setQuantity(product.getQuantity() - item.getQuantity());
      product.setSalesCount(product.getSalesCount() + item.getQuantity());
      productRepository.save(product);
    }

    // 支付成功后立即奖励积分（1元=1分，向下取整）
    int pointsToAward = order.getTotalAmount().setScale(0, java.math.RoundingMode.DOWN).intValue();
    if (pointsToAward > 0) {
      pointsService.addPoints(order.getUser().getId(), pointsToAward, "订单支付奖励积分 (订单号: " + order.getOrderNumber() + ")");
    }
  }

  @Override
  @Transactional
  public void confirmReceipt(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

    if (order.getStatus() != OrderStatus.DELIVERED) {
      throw new IllegalStateException("订单状态不正确");
    }

    // 更新订单状态为已完成
    order.setStatus(OrderStatus.COMPLETED);
    order.setCompletionTime(LocalDateTime.now());
    orderRepository.save(order);

    // 结算卖家资金
    settleSellerFunds(orderId);

    // 奖励积分
    awardPointsForOrder(order);
  }

  @Override
  @Transactional
  public void settleSellerFunds(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

    List<PendingFund> pendingFunds = pendingFundRepository.findByOrderItemIn(order.getOrderItems());

    for (PendingFund fund : pendingFunds) {
      if (fund.getStatus() == PendingFundStatus.HELD) {
        // 计算实际结算金额（扣除平台费用）
        BigDecimal settlementAmount = fund.getAmount().subtract(fund.getPlatformFee());

        // 获取卖家钱包
        Wallet sellerWallet = walletService.getWalletByUser(fund.getSeller());

        // 更新卖家钱包余额
        sellerWallet.setBalance(sellerWallet.getBalance().add(settlementAmount));
        walletRepository.save(sellerWallet);

        // 记录交易（商家收入）
        Transaction transaction = new Transaction();
        transaction.setUser(fund.getSeller());
        transaction.setType(TransactionType.SALE_INCOME);
        transaction.setAmount(settlementAmount); // 正数，表示收入
        transaction.setBalanceAfter(sellerWallet.getBalance());
        transaction.setDescription("订单结算: " + order.getOrderNumber());
        transaction.setRelatedOrder(order);
        transaction.setRelatedPendingFund(fund);
        transactionRepository.save(transaction);

        // 更新待结算资金状态
        fund.setStatus(PendingFundStatus.RELEASED);
        pendingFundRepository.save(fund);
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> findInProgressOrdersByUser(User user) {
    return orderRepository.findByUserAndStatusIn(user,
        Arrays.asList(OrderStatus.PENDING_DELIVERY, OrderStatus.DELIVERED));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> findOrdersByUser(User user, Pageable pageable) {
    return orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> findPendingSettlementOrdersBySeller(Seller seller) {
    return orderRepository.findBySellerAndStatus(seller, OrderStatus.COMPLETED);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> findOrdersBySeller(Seller seller, String status, Pageable pageable) {
    log.info("Fetching orders for seller: {}, status: {}, page: {}", seller.getId(), status, pageable.getPageNumber());
    if (status != null && !status.isEmpty()) {
      try {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findBySellerAndStatus(seller, orderStatus, pageable);
      } catch (IllegalArgumentException e) {
        log.warn("Invalid order status: {}", status);
        return Page.empty(pageable);
      }
    }
    return orderRepository.findBySellerOrderByCreatedAtDesc(seller, pageable);
  }

  @Override
  @Transactional
  public void awardPointsForOrder(Order order) {
    if (order.getStatus() == OrderStatus.COMPLETED) {
      // 计算订单原价（加上积分抵扣和优惠券折扣）
      BigDecimal originalAmount = order.getTotalAmount()
          .add(order.getDeductedAmount()) // 加上积分抵扣的金额
          .add(order.getOrderItems().stream() // 加上优惠券折扣的金额
              .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .subtract(order.getTotalAmount().add(order.getDeductedAmount())));

      // 计算应得积分（原价的1%）
      int pointsToAward = originalAmount.multiply(BigDecimal.valueOf(0.01)).intValue();

      if (pointsToAward > 0) {
        pointsService.addPoints(order.getUser().getId(), pointsToAward,
            String.format("订单完成奖励积分 (订单号: %s)", order.getOrderNumber()));
      }
    }
  }

  @Override
  @Transactional
  public Order createOrderFromCart(User user, int pointsToUse, Long userCouponId, boolean isOfflineTransaction,
      String offlineLocation,
      String offlineTime) {
    // 验证积分
    if (pointsToUse > 0 && !pointsService.hasEnoughPoints(user.getId(), pointsToUse)) {
      throw new IllegalArgumentException("积分不足");
    }

    List<CartItem> cartItems = cartItemRepository.findByUser(user);
    if (cartItems.isEmpty()) {
      throw new IllegalStateException("购物车为空");
    }

    // 创建订单
    Order order = new Order();
    order.setUser(user);
    order.setOrderNumber(generateOrderNumber());
    order.setStatus(OrderStatus.PENDING_PAYMENT);
    order.setCreatedAt(LocalDateTime.now());

    // 设置交易方式
    if (isOfflineTransaction) {
      order.setAgreedOfflineLocation(offlineLocation);
      order.setAgreedOfflineTime(offlineTime);
    }

    BigDecimal totalAmount = BigDecimal.ZERO;

    // 创建订单项
    List<OrderItem> orderItems = new ArrayList<>();
    for (CartItem cartItem : cartItems) {
      OrderItem orderItem = new OrderItem();
      orderItem.setOrder(order);
      orderItem.setProduct(cartItem.getProduct());
      orderItem.setSeller(cartItem.getProduct().getSeller());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setPrice(cartItem.getProduct().getDiscountPrice());
      orderItem.setProductName(cartItem.getProduct().getName());
      orderItems.add(orderItem);

      // 计算总金额
      totalAmount = totalAmount.add(cartItem.getProduct().getDiscountPrice()
          .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
    }

    // 计算积分抵扣金额
    BigDecimal pointsDeduction = BigDecimal.ZERO;
    if (pointsToUse > 0) {
      pointsDeduction = pointsService.calculateDeductibleAmount(pointsToUse);
      // 扣除积分
      pointsService.deductPoints(user.getId(), pointsToUse,
          String.format("订单使用积分抵扣 (订单号: %s)", order.getOrderNumber()));
    }

    // 优惠券逻辑
    UserCoupon userCoupon = null;
    BigDecimal couponDiscount = BigDecimal.ZERO;
    if (userCouponId != null) {
      userCoupon = couponService.findUserCouponById(userCouponId)
          .orElseThrow(() -> new IllegalArgumentException("优惠券不存在"));
      if (!userCoupon.getUser().getId().equals(user.getId())) {
        throw new IllegalArgumentException("不能使用他人的优惠券");
      }
      if (!couponService.validateCoupon(userCoupon, totalAmount)) {
        throw new IllegalArgumentException("优惠券不可用");
      }
      couponDiscount = couponService.calculateDiscount(userCoupon, totalAmount);
    }

    totalAmount = totalAmount.subtract(pointsDeduction).subtract(couponDiscount);

    order.setPointsDeducted(pointsToUse);
    order.setDeductedAmount(pointsDeduction);
    order.setTotalAmount(totalAmount);
    order.setOrderItems(orderItems);

    Order savedOrder = orderRepository.save(order);
    orderItems.forEach(item -> item.setOrder(savedOrder));
    orderItemRepository.saveAll(orderItems);

    cartItemRepository.deleteAll(cartItems);

    // 标记优惠券为已用
    if (userCoupon != null) {
      couponService.useCoupon(userCoupon, savedOrder.getId());
    }

    return savedOrder;
  }

  private String generateOrderNumber() {
    return "ORDER" + System.currentTimeMillis() +
        String.format("%04d", new Random().nextInt(10000));
  }

  @Override
  @Transactional(readOnly = true)
  public long countOrdersByUser(User user) {
    return orderRepository.countByUser(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderDTO> getUserOrders(String email, String status, int page) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Order> orders;

    if (status != null && !status.isEmpty()) {
      OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
      orders = orderRepository.findByUserAndStatus(user, orderStatus, pageRequest);
    } else {
      orders = orderRepository.findByUserOrderByCreatedAtDesc(user, pageRequest);
    }

    return orders.map(this::convertToDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderDTO getOrderDetail(Long id, String email) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Order order = orderRepository.findByIdAndUser(id, user)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    return convertToDTO(order);
  }

  @Override
  @Transactional(readOnly = true)
  public double calculateTotalAmount(String email) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    List<CartItem> cartItems = cartItemRepository.findByUser(user);
    return cartItems.stream()
        .mapToDouble(item -> item.getProduct().getDiscountPrice().doubleValue() * item.getQuantity())
        .sum();
  }

  @Override
  @Transactional
  public ApiResponse<OrderDTO> createOrder(OrderCreateRequest request, String email, Long userCouponId) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    try {
      Order order;
      if (request.getProductId() != null) {
        // Direct purchase
        order = createDirectOrder(user, request.getProductId(), request.getQuantity(),
            request.isUsePoints() ? user.getPoints().getBalance() : 0,
            userCouponId,
            "offline".equals(request.getShippingType()),
            request.getOfflineLocation(),
            request.getOfflineTime());
      } else {
        // Cart purchase
        order = createOrderFromCart(user, request.isUsePoints() ? user.getPoints().getBalance() : 0,
            userCouponId,
            "offline".equals(request.getShippingType()),
            request.getOfflineLocation(),
            request.getOfflineTime());
      }

      return ApiResponse.success("Order created successfully", convertToDTO(order));
    } catch (Exception e) {
      log.error("Error creating order for user {}: {}", email, e.getMessage(), e);
      return ApiResponse.error(e.getMessage());
    }
  }

  @Transactional
  public Order createDirectOrder(User user, Long productId, Integer quantity, int pointsToUse, Long userCouponId,
      boolean isOfflineTransaction, String offlineLocation, String offlineTime) {
    // 验证积分
    if (pointsToUse > 0 && !pointsService.hasEnoughPoints(user.getId(), pointsToUse)) {
      throw new IllegalArgumentException("积分不足");
    }

    // 获取商品信息
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    // 验证库存
    if (product.getQuantity() < quantity) {
      throw new IllegalArgumentException("商品库存不足");
    }

    // 创建订单
    Order order = new Order();
    order.setUser(user);
    order.setOrderNumber(generateOrderNumber());
    order.setStatus(OrderStatus.PENDING_PAYMENT);
    order.setCreatedAt(LocalDateTime.now());

    // 设置交易方式
    if (isOfflineTransaction) {
      order.setAgreedOfflineLocation(offlineLocation);
      order.setAgreedOfflineTime(offlineTime);
    }

    // 创建订单项
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(product);
    orderItem.setSeller(product.getSeller());
    orderItem.setQuantity(quantity);
    orderItem.setPrice(product.getDiscountPrice());
    orderItem.setProductName(product.getName());

    // 计算总金额
    BigDecimal totalAmount = product.getDiscountPrice().multiply(BigDecimal.valueOf(quantity));

    // 计算积分抵扣金额
    BigDecimal pointsDeduction = BigDecimal.ZERO;
    if (pointsToUse > 0) {
      pointsDeduction = pointsService.calculateDeductibleAmount(pointsToUse);
      // 扣除积分
      pointsService.deductPoints(user.getId(), pointsToUse,
          String.format("订单使用积分抵扣 (订单号: %s)", order.getOrderNumber()));
    }

    // 优惠券逻辑
    UserCoupon userCoupon = null;
    BigDecimal couponDiscount = BigDecimal.ZERO;
    if (userCouponId != null) {
      userCoupon = couponService.findUserCouponById(userCouponId)
          .orElseThrow(() -> new IllegalArgumentException("优惠券不存在"));
      if (!userCoupon.getUser().getId().equals(user.getId())) {
        throw new IllegalArgumentException("不能使用他人的优惠券");
      }
      if (!couponService.validateCoupon(userCoupon, totalAmount)) {
        throw new IllegalArgumentException("优惠券不可用");
      }
      couponDiscount = couponService.calculateDiscount(userCoupon, totalAmount);
    }

    totalAmount = totalAmount.subtract(pointsDeduction).subtract(couponDiscount);

    order.setPointsDeducted(pointsToUse);
    order.setDeductedAmount(pointsDeduction);
    order.setTotalAmount(totalAmount);
    order.getOrderItems().add(orderItem);

    Order savedOrder = orderRepository.save(order);
    orderItemRepository.save(orderItem);

    // 标记优惠券为已用
    if (userCoupon != null) {
      couponService.useCoupon(userCoupon, savedOrder.getId());
    }

    return savedOrder;
  }

  @Override
  @Transactional
  public ApiResponse<Void> confirmReceipt(Long id, String email) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Order order = orderRepository.findByIdAndUser(id, user)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (order.getStatus() != OrderStatus.DELIVERED) {
      return ApiResponse.<Void>error("Order is not in delivered status");
    }

    order.setStatus(OrderStatus.COMPLETED);
    order.setCompletionTime(LocalDateTime.now());
    orderRepository.save(order);

    // Award points
    awardPointsForOrder(order);

    // Settle seller funds
    settleSellerFunds(order.getId());

    return ApiResponse.<Void>success("Order completed successfully", null);
  }

  @Override
  @Transactional
  public ApiResponse<Void> cancelOrder(Long id, String email) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Order order = orderRepository.findByIdAndUser(id, user)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
      return ApiResponse.<Void>error("Order cannot be cancelled in current status");
    }

    // 如果使用了积分，返还积分
    if (order.getPointsDeducted() > 0) {
      pointsService.addPoints(user.getId(), order.getPointsDeducted(),
          String.format("订单取消退回积分 (订单号: %s)", order.getOrderNumber()));
    }

    // 删除订单项
    orderItemRepository.deleteAll(order.getOrderItems());
    // 删除订单
    orderRepository.delete(order);

    return ApiResponse.<Void>success("Order cancelled successfully", null);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> findBySellerAndStatus(Seller seller, OrderStatus status) {
    return orderRepository.findBySellerAndStatus(seller, status);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> findBySellerAndStatusAndCreatedAtAfter(Seller seller, OrderStatus status, LocalDateTime date) {
    return orderRepository.findBySellerAndStatusAndCreatedAtAfter(seller, status, date);
  }

  @Override
  @Transactional
  public ApiResponse<Void> submitReturnRequest(Long orderId, Long itemId, ReturnRequestDTO returnRequest,
      String email) {
    // 获取订单和验证所有权
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (!order.getUser().getEmail().equals(email)) {
      throw new AccessDeniedException("Not authorized to access this order");
    }

    // 获取订单项
    OrderItem orderItem = order.getOrderItems().stream()
        .filter(item -> item.getId().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

    // 验证订单状态
    if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.COMPLETED) {
      return ApiResponse.error("只有已收货的订单才能申请退货");
    }

    // 检查是否在24小时内
    if (order.getDeliveryTime() == null) {
      return ApiResponse.error("订单收货时间异常");
    }

    LocalDateTime returnDeadline = order.getDeliveryTime().plusHours(24);
    if (LocalDateTime.now().isAfter(returnDeadline)) {
      return ApiResponse.error("已超过退货申请时限（收货后24小时内）");
    }

    // 检查订单项状态
    if (orderItem.getStatus() != OrderItemStatus.NORMAL) {
      return ApiResponse.error("该商品已申请过退货");
    }

    // 创建退货申请记录
    ReturnRequest newReturnRequest = new ReturnRequest();
    newReturnRequest.setOrder(order);
    newReturnRequest.setUser(order.getUser());
    newReturnRequest.setReason(ReturnRequest.ReturnReason.valueOf(returnRequest.getReason()));
    newReturnRequest.setDescription(returnRequest.getDescription());
    newReturnRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);

    // 创建退货申请项记录
    ReturnRequestItem returnRequestItem = new ReturnRequestItem();
    returnRequestItem.setReturnRequest(newReturnRequest);
    returnRequestItem.setOrderItem(orderItem);
    returnRequestItem.setQuantity(orderItem.getQuantity());

    // 更新订单和订单项状态
    order.setStatus(OrderStatus.RETURN_REQUESTED);
    orderItem.setStatus(OrderItemStatus.RETURN_REQUESTED);
    order.setReturnRequestTime(LocalDateTime.now());

    // 保存所有更改
    returnRequestRepository.save(newReturnRequest);
    returnRequestItemRepository.save(returnRequestItem);
    orderRepository.save(order);
    orderItemRepository.save(orderItem);

    return ApiResponse.success("退货申请已提交，请等待商家审核");
  }

  @Override
  @Transactional
  public ApiResponse<Void> processReturnRequest(Long requestId, boolean approved, String sellerEmail) {
    ReturnRequest request = returnRequestRepository.findById(requestId)
        .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));

    // 权限校验：判断该退货请求的商品是否属于当前卖家
    boolean hasAccess = request.getReturnRequestItems().stream()
        .anyMatch(ri -> ri.getOrderItem().getSeller().getEmail().equals(sellerEmail));
    if (!hasAccess) {
      throw new AccessDeniedException("Not authorized to process this return request");
    }

    // 只允许处理PENDING状态
    if (request.getStatus() != ReturnRequest.ReturnStatus.PENDING) {
      return ApiResponse.error("退货请求状态不正确");
    }

    if (approved) {
      request.setStatus(ReturnRequest.ReturnStatus.APPROVED);
      // 订单状态也可同步变更
      request.getOrder().setStatus(OrderStatus.RETURN_APPROVED);
      // 退款逻辑
      handleRefund(request.getOrder());
    } else {
      request.setStatus(ReturnRequest.ReturnStatus.REJECTED);
      request.getOrder().setStatus(OrderStatus.RETURN_REJECTED);
    }

    returnRequestRepository.save(request);
    // 订单状态同步保存
    orderRepository.save(request.getOrder());

    return ApiResponse.success(approved ? "退货申请已通过" : "退货申请已拒绝");
  }

  @Override
  @Transactional
  public ApiResponse<Void> deliverOrder(Long orderId, String email) {
    // 获取订单
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    // 验证卖家权限
    if (!order.getOrderItems().stream()
        .anyMatch(item -> item.getSeller().getEmail().equals(email))) {
      throw new AccessDeniedException("Not authorized to deliver this order");
    }

    // 验证订单状态
    if (order.getStatus() != OrderStatus.IN_TRANSIT) {
      return ApiResponse.error("订单状态不正确，无法确认送达");
    }

    // 更新订单状态
    order.setStatus(OrderStatus.DELIVERED);
    order.setDeliveryTime(LocalDateTime.now());
    orderRepository.save(order);

    return ApiResponse.success("订单已确认送达");
  }

  private OrderDTO convertToDTO(Order order) {
    OrderDTO dto = new OrderDTO();
    dto.setId(order.getId());
    dto.setOrderNumber(order.getOrderNumber());
    dto.setStatus(order.getStatus().toString());
    dto.setTotalAmount(order.getTotalAmount().doubleValue());
    dto.setDeductedAmount(order.getDeductedAmount().doubleValue());
    dto.setPointsDeducted(order.getPointsDeducted());
    dto.setAgreedOfflineTime(order.getAgreedOfflineTime());
    dto.setAgreedOfflineLocation(order.getAgreedOfflineLocation());
    dto.setCreatedAt(order.getCreatedAt());
    dto.setUpdatedAt(order.getUpdatedAt());
    dto.setPaymentTime(order.getPaymentTime());
    dto.setDeliveryTime(order.getDeliveryTime());
    dto.setCompletionTime(order.getCompletionTime());

    // 动态计算优惠券抵扣金额
    double couponDiscount = 0.0;
    // 订单项总价+积分抵扣前金额
    double originalAmount = order.getOrderItems().stream()
        .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
        .sum();
    double afterPoints = originalAmount - order.getDeductedAmount().doubleValue();
    if (afterPoints > order.getTotalAmount().doubleValue()) {
      couponDiscount = afterPoints - order.getTotalAmount().doubleValue();
    }
    dto.setCouponDiscount(couponDiscount);

    List<OrderItemDTO> items = order.getOrderItems().stream()
        .map(item -> {
          OrderItemDTO itemDTO = new OrderItemDTO();
          itemDTO.setId(item.getId());
          itemDTO.setProductId(item.getProduct().getId());
          itemDTO.setProductName(item.getProductName());
          itemDTO.setProductImageUrl(
              item.getProduct().getPrimaryImage() != null ? item.getProduct().getPrimaryImage().getImageUrl() : null);
          itemDTO.setQuantity(item.getQuantity());
          itemDTO.setPrice(item.getPrice());
          itemDTO.setSellerId(item.getSeller().getId());
          itemDTO.setSellerName(item.getSeller().getUsername());
          itemDTO.setStatus(item.getStatus().toString());
          itemDTO.setReturnReason(item.getStatus() == OrderItemStatus.RETURN_REQUESTED ? "退货申请中" : null);
          return itemDTO;
        })
        .collect(Collectors.toList());
    dto.setItems(items);

    return dto;
  }

  @Override
  public String createPayment(Order order) {
    // 直接调用支付方法
    payOrder(order);
    return "/orders/" + order.getId();
  }

  @Override
  public BigDecimal calculateProductTotal(Long productId, Integer quantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    BigDecimal price = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getOriginalPrice();

    return price.multiply(BigDecimal.valueOf(quantity));
  }
}
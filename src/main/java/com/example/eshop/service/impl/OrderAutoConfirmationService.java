package com.example.eshop.service.impl;

import com.example.eshop.model.Order;
import com.example.eshop.model.OrderStatus;
import com.example.eshop.repository.OrderRepository;
import com.example.eshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderAutoConfirmationService {

  private static final Logger log = LoggerFactory.getLogger(OrderAutoConfirmationService.class);
  private final OrderRepository orderRepository;
  private final OrderService orderService;

  @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
  @Transactional
  public void autoConfirmOrders() {
    log.info("Starting auto confirmation of orders...");
    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

    // 查找所有已发货超过7天的订单
    List<Order> ordersToConfirm = orderRepository.findByStatusAndDeliveryTimeBefore(
        OrderStatus.DELIVERED, sevenDaysAgo);

    for (Order order : ordersToConfirm) {
      try {
        log.info("Auto confirming order: {}", order.getOrderNumber());
        order.setStatus(OrderStatus.AUTO_COMPLETED);
        order.setCompletionTime(LocalDateTime.now());
        orderRepository.save(order);

        // 结算卖家资金
        orderService.settleSellerFunds(order.getId());

        // 奖励积分
        orderService.awardPointsForOrder(order);

        log.info("Successfully auto confirmed order: {}", order.getOrderNumber());
      } catch (Exception e) {
        log.error("Error auto confirming order {}: {}", order.getOrderNumber(), e.getMessage(), e);
      }
    }
    log.info("Completed auto confirmation of orders. Processed {} orders", ordersToConfirm.size());
  }
}
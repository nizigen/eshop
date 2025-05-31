package com.example.eshop.service;

import com.example.eshop.model.Points;
import com.example.eshop.model.PointsHistory;
import com.example.eshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface PointsService {
  /**
   * 获取用户积分历史记录
   */
  Page<PointsHistory> getPointsHistory(Long userId, Pageable pageable);

  /**
   * 获取用户积分
   */
  Points getPointsByUser(User user);

  /**
   * 获取用户积分数量
   */
  Integer getPoints(Long userId);

  /**
   * 增加用户积分
   */
  void addPoints(Long userId, Integer points, String description);

  /**
   * 扣减用户积分
   */
  void deductPoints(Long userId, Integer points, String description);

  /**
   * 计算消费可获得的积分数
   */
  int calculatePointsForPurchase(BigDecimal amount);

  /**
   * 计算积分可抵扣的金额
   */
  BigDecimal calculateDeductibleAmount(int points);

  /**
   * 检查用户是否有足够的积分
   */
  boolean hasEnoughPoints(Long userId, int requiredPoints);

  int getAvailablePoints(User user);

  void deductPoints(User user, int points);

  void addPoints(User user, int points);
}
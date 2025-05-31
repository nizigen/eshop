package com.example.eshop.service.impl;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.Points;
import com.example.eshop.model.PointsHistory;
import com.example.eshop.model.PointsHistoryType;
import com.example.eshop.model.User;
import com.example.eshop.repository.PointsHistoryRepository;
import com.example.eshop.repository.PointsRepository;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PointsServiceImpl implements PointsService {

  private final PointsHistoryRepository pointsHistoryRepository;
  private final PointsRepository pointsRepository;
  private final UserRepository userRepository;

  // 积分兑换比率：100积分 = 1元
  private static final BigDecimal POINTS_TO_MONEY_RATIO = new BigDecimal("0.01");
  // 消费获得积分比率：1元 = 1积分
  private static final BigDecimal MONEY_TO_POINTS_RATIO = new BigDecimal("1");

  @Autowired
  public PointsServiceImpl(PointsHistoryRepository pointsHistoryRepository,
      PointsRepository pointsRepository,
      UserRepository userRepository) {
    this.pointsHistoryRepository = pointsHistoryRepository;
    this.pointsRepository = pointsRepository;
    this.userRepository = userRepository;
  }

  @Override
  public Page<PointsHistory> getPointsHistory(Long userId, Pageable pageable) {
    return pointsHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Override
  public Points getPointsByUser(User user) {
    return pointsRepository.findById(user.getId())
        .orElseGet(() -> {
          Points points = new Points();
          points.setUser(user);
          return pointsRepository.save(points);
        });
  }

  @Override
  public Integer getPoints(Long userId) {
    return pointsRepository.findById(userId)
        .map(Points::getBalance)
        .orElse(0);
  }

  @Override
  @Transactional
  public void addPoints(Long userId, Integer points, String description) {
    // EARN类型，points必须为正数
    if (points <= 0) {
      throw new IllegalArgumentException("Points must be positive");
    }

    Points userPoints = pointsRepository.findById(userId)
        .orElseGet(() -> {
          User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("User not found"));
          Points newPoints = new Points();
          newPoints.setUser(user);
          return newPoints;
        });

    userPoints.setBalance(userPoints.getBalance() + points);
    pointsRepository.save(userPoints);

    PointsHistory history = new PointsHistory();
    history.setUser(userPoints.getUser());
    history.setPoints(points); // EARN为正数
    history.setDescription(description);
    history.setType(PointsHistoryType.EARN);
    pointsHistoryRepository.save(history);
  }

  @Override
  @Transactional
  public void deductPoints(Long userId, Integer points, String description) {
    // DEDUCT类型，points必须为正数，实际记录为负数
    if (points <= 0) {
      throw new IllegalArgumentException("Points must be positive");
    }

    Points userPoints = pointsRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User points not found"));

    if (userPoints.getBalance() < points) {
      throw new IllegalArgumentException("Insufficient points");
    }

    userPoints.setBalance(userPoints.getBalance() - points);
    pointsRepository.save(userPoints);

    PointsHistory history = new PointsHistory();
    history.setUser(userPoints.getUser());
    history.setPoints(-points); // DEDUCT为负数
    history.setDescription(description);
    history.setType(PointsHistoryType.DEDUCT);
    pointsHistoryRepository.save(history);
  }

  @Override
  public int calculatePointsForPurchase(BigDecimal amount) {
    return amount.multiply(MONEY_TO_POINTS_RATIO)
        .setScale(0, RoundingMode.DOWN)
        .intValue();
  }

  @Override
  public BigDecimal calculateDeductibleAmount(int points) {
    return BigDecimal.valueOf(points).multiply(POINTS_TO_MONEY_RATIO)
        .setScale(2, RoundingMode.DOWN);
  }

  @Override
  public boolean hasEnoughPoints(Long userId, int requiredPoints) {
    return getPoints(userId) >= requiredPoints;
  }

  @Override
  public int getAvailablePoints(User user) {
    return getPoints(user.getId());
  }

  @Override
  @Transactional
  public void deductPoints(User user, int points) {
    deductPoints(user.getId(), points, "Points deducted");
  }

  @Override
  @Transactional
  public void addPoints(User user, int points) {
    addPoints(user.getId(), points, "Points added");
  }
}
package com.example.eshop.service.impl;

import com.example.eshop.model.SellerBuyerReview;
import com.example.eshop.model.Order;
import com.example.eshop.model.User;
import com.example.eshop.repository.SellerBuyerReviewRepository;
import com.example.eshop.service.SellerBuyerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerBuyerReviewServiceImpl implements SellerBuyerReviewService {
  private final SellerBuyerReviewRepository repository;

  @Override
  @Transactional
  public SellerBuyerReview addReview(Order order, User seller, User buyer, Boolean isPositive, String comment) {
    // 防止重复回评
    if (repository.findByOrderAndSeller(order, seller).isPresent()) {
      throw new IllegalStateException("已评价该买家");
    }
    SellerBuyerReview review = new SellerBuyerReview();
    review.setOrder(order);
    review.setSeller(seller);
    review.setBuyer(buyer);
    review.setIsPositive(isPositive);
    review.setComment(comment);
    return repository.save(review);
  }

  @Override
  public Optional<SellerBuyerReview> findByOrderAndSeller(Order order, User seller) {
    return repository.findByOrderAndSeller(order, seller);
  }
}
package com.example.eshop.service;

import com.example.eshop.model.SellerBuyerReview;
import com.example.eshop.model.Order;
import com.example.eshop.model.User;

import java.util.Optional;

public interface SellerBuyerReviewService {
  SellerBuyerReview addReview(Order order, User seller, User buyer, Boolean isPositive, String comment);

  Optional<SellerBuyerReview> findByOrderAndSeller(Order order, User seller);
}
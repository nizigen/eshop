package com.example.eshop.repository;

import com.example.eshop.model.SellerBuyerReview;
import com.example.eshop.model.Order;
import com.example.eshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerBuyerReviewRepository extends JpaRepository<SellerBuyerReview, Long> {
  Optional<SellerBuyerReview> findByOrderAndSeller(Order order, User seller);
}
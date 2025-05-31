package com.example.eshop.repository;

import com.example.eshop.model.OrderItem;
import com.example.eshop.model.PendingFund;
import com.example.eshop.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingFundRepository extends JpaRepository<PendingFund, Long> {
  List<PendingFund> findByOrderItemIn(List<OrderItem> orderItems);

  List<PendingFund> findBySeller(Seller seller);
}
package com.example.eshop.repository;

import com.example.eshop.model.SellerFeeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SellerFeeRateRepository extends JpaRepository<SellerFeeRate, Integer> {
  Optional<SellerFeeRate> findByLevel(Integer level);
}
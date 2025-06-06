package com.example.eshop.repository;

import com.example.eshop.model.Product;
import com.example.eshop.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
  List<ProductImage> findByProduct(Product product);
}
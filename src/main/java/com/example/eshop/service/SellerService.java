package com.example.eshop.service;

import com.example.eshop.model.Seller;
import com.example.eshop.model.SellerDetails;
import com.example.eshop.dto.SellerDTO;
import com.example.eshop.enums.SellerStatus;
import java.util.List;
import java.util.Optional;

public interface SellerService {
  // Basic CRUD operations
  Optional<Seller> findById(Long id);

  List<Seller> findAllSellers();

  Seller saveSeller(Seller seller);

  void deleteSeller(Long id);

  // Store related operations
  void updateStoreName(Long sellerId, String storeName);

  void updateStoreDescription(Long sellerId, String storeDescription);

  void updateBusinessLicense(Long sellerId, String businessLicenseUrl);

  // Seller details operations
  Optional<SellerDetails> findSellerDetails(Long sellerId);

  SellerDetails updateSellerDetails(Long sellerId, SellerDetails details);

  // Statistics and metrics
  double getAverageServiceRating(Long sellerId);

  double getBuyerPositiveRatingPercent(Long sellerId);

  int getSellerLevel(Long sellerId);

  // Search operations
  List<Seller> findSellersByStoreName(String storeName);

  List<Seller> findSellersByLevel(int level);

  List<Seller> findTopRatedSellers(int limit);

  List<SellerDTO> getAllSellers();

  void updateSellerStatus(Long sellerId, SellerStatus status);

  void updateSellerLevel(Long sellerId, Integer level);
}
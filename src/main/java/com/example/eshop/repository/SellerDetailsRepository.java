package com.example.eshop.repository;

import com.example.eshop.model.SellerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SellerDetailsRepository extends JpaRepository<SellerDetails, Long> {
  List<SellerDetails> findByStoreName(String storeName);

  List<SellerDetails> findBySellerLevel(Integer level);

  @Query("SELECT sd FROM SellerDetails sd WHERE sd.serviceRating >= :minRating")
  List<SellerDetails> findByMinimumServiceRating(double minRating);

  @Query("SELECT sd FROM SellerDetails sd ORDER BY sd.serviceRating DESC")
  List<SellerDetails> findAllOrderByServiceRatingDesc();

  @Query("SELECT sd FROM SellerDetails sd WHERE LOWER(sd.storeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(sd.storeDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<SellerDetails> searchByKeyword(String keyword);

  @Modifying
  @Query("UPDATE SellerDetails s SET s.serviceRating = :rating WHERE s.userId = :sellerId")
  void updateServiceRating(@Param("sellerId") Long sellerId, @Param("rating") double rating);

  @Query("SELECT s FROM SellerDetails s WHERE s.userId = :sellerId")
  SellerDetails findByUserId(@Param("sellerId") Long sellerId);

  List<SellerDetails> findByStoreNameContaining(String storeName);

  @Query("SELECT sd FROM SellerDetails sd ORDER BY sd.serviceRating DESC LIMIT :limit")
  List<SellerDetails> findTopByServiceRatingOrderByServiceRatingDesc(@Param("limit") int limit);
}
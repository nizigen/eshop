package com.example.eshop.repository;

import com.example.eshop.model.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
  Page<ReturnRequest> findByUserId(Long userId, Pageable pageable);

  @Query("SELECT DISTINCT rr FROM ReturnRequest rr " +
      "JOIN rr.returnRequestItems ri " +
      "JOIN ri.orderItem oi " +
      "JOIN oi.seller s " +
      "WHERE s.id = :sellerId " +
      "AND rr.status = 'PENDING' " +
      "ORDER BY rr.createdAt DESC")
  Page<ReturnRequest> findByOrderItemsSellerId(@Param("sellerId") Long sellerId, Pageable pageable);

  @Query("SELECT COUNT(rr) FROM ReturnRequest rr " +
      "JOIN rr.returnRequestItems ri " +
      "JOIN ri.orderItem oi " +
      "JOIN oi.seller s " +
      "WHERE s.id = :sellerId " +
      "AND rr.status = 'PENDING'")
  long countPendingReturnsBySellerId(@Param("sellerId") Long sellerId);
}
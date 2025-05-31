package com.example.eshop.repository;

import com.example.eshop.model.Transaction;
import com.example.eshop.model.TransactionType;
import com.example.eshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  Page<Transaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  Page<Transaction> findByUserAndTypeOrderByCreatedAtDesc(User user, TransactionType type,
      Pageable pageable);

  @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.createdAt BETWEEN :startDate AND :endDate")
  BigDecimal sumAmountByUserAndTypeAndDateBetween(User user, TransactionType type, LocalDateTime startDate,
      LocalDateTime endDate);

  List<Transaction> findByUserAndTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
      User user, TransactionType type, LocalDateTime startDate, LocalDateTime endDate);

  List<Transaction> findByUserAndTypeAndCreatedAtBetween(
      User user,
      TransactionType type,
      LocalDateTime start,
      LocalDateTime end);
}
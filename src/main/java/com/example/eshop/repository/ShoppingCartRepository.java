package com.example.eshop.repository;

import com.example.eshop.model.ShoppingCartItem;
import com.example.eshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCartItem, Long> {

  List<ShoppingCartItem> findByUser(User user);

  List<ShoppingCartItem> findBySessionId(String sessionId);

  Optional<ShoppingCartItem> findByUserAndProductId(User user, Long productId);

  Optional<ShoppingCartItem> findBySessionIdAndProductId(String sessionId, Long productId);

  void deleteByUser(User user);

  void deleteBySessionId(String sessionId);

  @Query("SELECT COUNT(sci) FROM ShoppingCartItem sci WHERE sci.user = :user")
  int countByUser(@Param("user") User user);

  @Query("SELECT COUNT(sci) FROM ShoppingCartItem sci WHERE sci.sessionId = :sessionId")
  int countBySessionId(@Param("sessionId") String sessionId);

  @Query("SELECT COALESCE(SUM(sci.quantity), 0) FROM ShoppingCartItem sci WHERE sci.user = :user")
  int sumQuantitiesByUser(@Param("user") User user);

  @Query("SELECT COALESCE(SUM(sci.quantity), 0) FROM ShoppingCartItem sci WHERE sci.sessionId = :sessionId")
  int sumQuantitiesBySessionId(@Param("sessionId") String sessionId);
}
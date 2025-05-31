package com.example.eshop.repository;

import com.example.eshop.model.Order;
import com.example.eshop.model.OrderStatus;
import com.example.eshop.model.Seller;
import com.example.eshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  // User queries
  Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

  Optional<Order> findByIdAndUser(Long id, User user);

  long countByUser(User user);

  List<Order> findByUserAndStatusIn(User user, List<OrderStatus> statuses);

  // Seller queries
  @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems i WHERE i.seller = :seller AND o.status = :status")
  List<Order> findBySellerAndStatus(@Param("seller") Seller seller, @Param("status") OrderStatus status);

  @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems i WHERE i.seller = :seller AND o.status = :status")
  Page<Order> findBySellerAndStatus(@Param("seller") Seller seller, @Param("status") OrderStatus status,
      Pageable pageable);

  @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderItems i WHERE i.seller = :seller")
  long countBySeller(@Param("seller") Seller seller);

  @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderItems i WHERE i.seller = :seller AND o.status = :status")
  long countBySellerAndStatus(@Param("seller") Seller seller, @Param("status") OrderStatus status);

  @Query(value = "SELECT DISTINCT o FROM Order o JOIN o.orderItems i WHERE i.seller = :seller ORDER BY o.createdAt DESC")
  Page<Order> findBySellerOrderByCreatedAtDesc(@Param("seller") Seller seller, Pageable pageable);

  @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems i WHERE i.seller = :seller AND o.status = :status AND o.createdAt > :date")
  List<Order> findBySellerAndStatusAndCreatedAtAfter(@Param("seller") Seller seller,
      @Param("status") OrderStatus status, @Param("date") LocalDateTime date);

  @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems i WHERE i.seller = :seller")
  List<Order> findBySeller(@Param("seller") Seller seller);

  // 查找已发货超过指定时间的订单
  List<Order> findByStatusAndDeliveryTimeBefore(OrderStatus status, LocalDateTime time);

  // 查找指定状态的订单
  List<Order> findByStatus(OrderStatus status);

  // 根据订单号查找订单
  Optional<Order> findByOrderNumber(String orderNumber);
}
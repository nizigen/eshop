package com.example.eshop.repository;

import com.example.eshop.model.User;
import com.example.eshop.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

  List<UserAddress> findByUserOrderByDefaultAddressDescCreatedAtDesc(User user);

  Optional<UserAddress> findByUserAndDefaultAddressTrue(User user);

  @Modifying
  @Query("UPDATE UserAddress a SET a.defaultAddress = false WHERE a.user = :user AND a.id != :addressId")
  void unsetOtherDefaultAddresses(User user, Long addressId);

  boolean existsByUserAndDefaultAddressTrue(User user);

  @Modifying
  @Query("DELETE FROM UserAddress a WHERE a.user = :user AND a.id = :addressId")
  void deleteByUserAndId(User user, Long addressId);

  @Query("SELECT a FROM UserAddress a WHERE a.user = :user AND a.id = :addressId")
  Optional<UserAddress> findByUserAndId(User user, Long addressId);
}
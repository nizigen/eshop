package com.example.eshop.repository;

import com.example.eshop.model.User;
import com.example.eshop.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  // Method to find a user by email (case-insensitive can be added if needed)
  Optional<User> findByEmail(String email);

  // Method to find a user by phone number
  Optional<User> findByPhone(String phone);

  // Method to find a user by username
  Optional<User> findByUsername(String username);

  // Method to check if a user exists by email
  boolean existsByEmail(String email);

  // Method to check if a user exists by phone number
  boolean existsByPhone(String phone);

  // Method to find users by status
  List<User> findByStatus(UserStatus status);

  // Method to check if a user exists by username
  boolean existsByUsername(String username);

  long countByStatus(UserStatus status);
}
package com.example.eshop.repository;

import com.example.eshop.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
  Optional<UserProfile> findByUserId(Long userId);

  void deleteByUserId(Long userId);
}
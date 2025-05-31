package com.example.eshop.repository;

import com.example.eshop.model.Points;
import com.example.eshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {
  Optional<Points> findByUser(User user);
}
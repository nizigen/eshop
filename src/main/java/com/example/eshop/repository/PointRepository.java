package com.example.eshop.repository;

import com.example.eshop.model.Point;
import com.example.eshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
  Optional<Point> findByUser(User user);
}
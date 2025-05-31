package com.example.eshop.repository;

import com.example.eshop.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  // Find category by name (useful for checking uniqueness or lookup)
  Optional<Category> findByNameIgnoreCase(String name);

  // Add other specific query methods if needed later
  // e.g., findAllByParentCategoryIsNull() for top-level categories

  List<Category> findByEnabledTrueOrderByDisplayOrderAsc();

  boolean existsByNameIgnoreCase(String name);

  @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL")
  List<Category> findAllWithChildren();
}
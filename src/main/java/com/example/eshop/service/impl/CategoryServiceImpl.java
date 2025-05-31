package com.example.eshop.service.impl;

import com.example.eshop.dto.CategoryDTO;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.Category;
import com.example.eshop.repository.CategoryRepository;
import com.example.eshop.repository.ProductRepository;
import com.example.eshop.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

  private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  @Autowired
  public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
    this.categoryRepository = categoryRepository;
    this.productRepository = productRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Category> findAllCategories() {
    log.info("Fetching all categories");
    return categoryRepository.findAllWithChildren();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CategoryDTO> findAllCategoriesWithProductCount() {
    return categoryRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  private CategoryDTO convertToDTO(Category category) {
    CategoryDTO dto = new CategoryDTO();
    dto.setId(category.getId());
    dto.setName(category.getName());
    dto.setDescription(category.getDescription());
    dto.setIconUrl(category.getIconUrl());
    dto.setDisplayOrder(category.getDisplayOrder());
    dto.setEnabled(category.getEnabled());
    dto.setProductCount(productRepository.countByCategory(category));

    if (category.getParent() != null) {
      dto.setParentId(category.getParent().getId());
      dto.setParentName(category.getParent().getName());
    }

    if (category.getChildren() != null) {
      dto.setChildren(category.getChildren().stream()
          .map(this::convertToDTO)
          .collect(Collectors.toList()));
    }

    return dto;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Category> findCategoryById(Long id) {
    log.info("Fetching category by ID: {}", id);
    return categoryRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Category findById(Long id) {
    log.info("Fetching category by ID: {}", id);
    return categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Category> findEnabledCategories() {
    log.info("Fetching enabled categories ordered by display order");
    return categoryRepository.findByEnabledTrueOrderByDisplayOrderAsc();
  }

  @Override
  @Transactional
  public Category saveCategory(Category category) {
    log.info("Saving category: {}", category.getName());
    Optional<Category> existing = categoryRepository.findByNameIgnoreCase(category.getName());
    if (existing.isPresent() && !existing.get().getId().equals(category.getId())) {
      log.warn("Category with name '{}' already exists.", category.getName());
      throw new DataIntegrityViolationException("Category name must be unique.");
    }
    return categoryRepository.save(category);
  }

  @Override
  @Transactional
  public void deleteCategory(Long id) {
    log.warn("Attempting to delete category with ID: {}", id);
    if (!categoryRepository.existsById(id)) {
      throw new ResourceNotFoundException("Category not found with id: " + id);
    }
    try {
      // Check if category is in use by products before deleting
      // This check should be implemented in ProductRepository
      categoryRepository.deleteById(id);
      log.info("Category deleted successfully with ID: {}", id);
    } catch (DataIntegrityViolationException e) {
      log.error("Cannot delete category ID {}: It is referenced by existing products.", id, e);
      throw new DataIntegrityViolationException("Cannot delete category: It is currently in use by products.", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Category> findCategoryByName(String name) {
    log.info("Fetching category by name: {}", name);
    return categoryRepository.findByNameIgnoreCase(name);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByName(String name) {
    return categoryRepository.existsByNameIgnoreCase(name);
  }
}
package com.example.eshop.service;

import com.example.eshop.model.Category;
import com.example.eshop.dto.CategoryDTO;
import java.util.List;
import java.util.Optional;

public interface CategoryService {

  /**
   * Retrieves all categories.
   * 
   * @return A list of all categories.
   */
  List<Category> findAllCategories();

  /**
   * Retrieves all categories with their product counts.
   * 
   * @return A list of CategoryDTO containing category information and product
   *         counts.
   */
  List<CategoryDTO> findAllCategoriesWithProductCount();

  /**
   * Finds a category by its ID.
   * 
   * @param id The ID of the category to find.
   * @return An Optional containing the category if found, otherwise empty.
   */
  Optional<Category> findCategoryById(Long id);

  /**
   * Finds a category by its ID and throws an exception if not found.
   * 
   * @param id The ID of the category to find.
   * @return The category if found.
   * @throws ResourceNotFoundException if the category is not found.
   */
  Category findById(Long id);

  /**
   * Retrieves all enabled categories ordered by display order.
   * 
   * @return A list of enabled categories.
   */
  List<Category> findEnabledCategories();

  /**
   * Saves a new category or updates an existing one.
   * 
   * @param category The category object to save.
   * @return The saved category entity.
   * @throws DataIntegrityViolationException if a category with the same name
   *                                         already exists.
   */
  Category saveCategory(Category category);

  /**
   * Deletes a category by its ID.
   * 
   * @param id The ID of the category to delete.
   * @throws ResourceNotFoundException       if the category is not found.
   * @throws DataIntegrityViolationException if the category is referenced by
   *                                         products.
   */
  void deleteCategory(Long id);

  /**
   * Finds a category by its name (case-insensitive).
   * 
   * @param name The name of the category to find.
   * @return An Optional containing the category if found, otherwise empty.
   */
  Optional<Category> findCategoryByName(String name);

  /**
   * Checks if a category with the given name exists.
   * 
   * @param name The name to check.
   * @return true if a category with the name exists, false otherwise.
   */
  boolean existsByName(String name);
}
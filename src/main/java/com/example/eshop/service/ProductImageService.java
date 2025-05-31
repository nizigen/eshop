package com.example.eshop.service;

import com.example.eshop.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductImageService {
  /**
   * Saves multiple image files associated with a product.
   * Implementation should handle file storage and create ProductImage entities.
   *
   * @param product The product to associate images with.
   * @param files   The list of image files to save.
   * @throws IOException If an error occurs during file storage.
   */
  void saveImagesForProduct(Product product, List<MultipartFile> files) throws IOException;

  /**
   * Deletes all image files and ProductImage entities associated with a product.
   * Used typically when the product itself is being deleted.
   *
   * @param product The product whose images should be deleted.
   * @throws IOException If an error occurs during file deletion.
   */
  void deleteImagesForProduct(Product product) throws IOException;

  /**
   * Deletes specific images by their IDs, ensuring they belong to the correct
   * product.
   * Also deletes the corresponding image files from storage.
   *
   * @param imageIds  A list of IDs of the ProductImage entities to delete.
   * @param productId The ID of the product these images should belong to (for
   *                  verification).
   * @throws IOException If an error occurs during file deletion.
   */
  void deleteImagesByIds(List<Long> imageIds, Long productId) throws IOException;

  // Potentially add methods to retrieve image URLs/paths for a product
}
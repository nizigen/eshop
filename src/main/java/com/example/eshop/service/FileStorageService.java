package com.example.eshop.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

  /**
   * Initializes the storage location.
   */
  void init() throws IOException;

  /**
   * Stores a file.
   *
   * @param file    The file to store.
   * @param subPath The relative sub-path (e.g., "products/1") where the file
   *                should be stored.
   *                A unique filename will be generated within this sub-path.
   * @return The unique relative path (including subPath and generated filename)
   *         where the file was stored.
   * @throws IOException if an error occurs during storage.
   */
  String store(MultipartFile file, String subPath) throws IOException;

  /**
   * Loads a file as a Resource based on its full relative path.
   *
   * @param relativePath The full relative path (e.g., "products/1/uuid.jpg") of
   *                     the file.
   * @return The Resource representing the file.
   * @throws IOException if the file cannot be loaded or does not exist.
   */
  Resource loadAsResource(String relativePath) throws IOException;

  /**
   * Deletes a file based on its full relative path.
   *
   * @param relativePath The full relative path of the file to delete.
   * @throws IOException if an error occurs during deletion.
   */
  void delete(String relativePath) throws IOException;

  /**
   * Deletes a directory and all its contents if it exists.
   *
   * @param relativePath The relative path of the directory to delete.
   * @throws IOException if an error occurs during deletion.
   */
  void deleteDirectory(String relativePath) throws IOException;

  /**
   * Gets the root storage location path.
   * 
   * @return The root path.
   */
  Path getRootLocation();

  /**
   * Store a file and return its URL/path
   * 
   * @param file The file to store
   * @return The URL/path where the file is stored
   * @throws IOException If there's an error storing the file
   */
  String storeFile(MultipartFile file) throws IOException;

  /**
   * Delete a stored file
   * 
   * @param fileUrl The URL/path of the file to delete
   * @return true if the file was deleted successfully, false otherwise
   */
  boolean deleteFile(String fileUrl);

  /**
   * Get the absolute path of a stored file
   * 
   * @param fileUrl The URL/path of the file
   * @return The absolute path of the file
   */
  String getFilePath(String fileUrl);
}
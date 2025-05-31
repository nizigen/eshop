package com.example.eshop.service;

import com.example.eshop.model.User;

import java.util.List;

public interface AdminService {

  List<User> findPendingUsers();

  void approveUser(Long userId);

  void rejectUser(Long userId);

  // Optional: Method to ban/delete/modify users might go here too
  List<User> findAllUsers(); // Example for user management

  void deleteUser(Long userId); // Example for user management

  // Ensure these methods are present for editing users
  User findUserById(Long userId);

  void updateUser(User user); // Or use a DTO for updates
}
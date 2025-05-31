package com.example.eshop.service.impl;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.User;
import com.example.eshop.model.UserStatus;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.service.AdminService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

  private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

  @Autowired
  private UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<User> findPendingUsers() {
    log.info("Finding users with PENDING status");
    return userRepository.findByStatus(UserStatus.PENDING);
  }

  @Override
  @Transactional
  public void approveUser(Long userId) {
    log.info("Attempting to approve user with ID: {}", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.error("User not found with ID: {} for approval", userId);
          return new ResourceNotFoundException("User not found with id: " + userId);
        });

    if (user.getStatus() == UserStatus.PENDING) {
      user.setStatus(UserStatus.ACTIVE);
      userRepository.save(user);
      log.info("User {} status set to ACTIVE", userId);
    } else {
      log.warn("User {} is not in PENDING status, cannot approve. Current status: {}", userId, user.getStatus());
      // Optionally throw an exception or just ignore
    }
  }

  @Override
  @Transactional
  public void rejectUser(Long userId) {
    log.info("Attempting to reject user with ID: {}", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.error("User not found with ID: {} for rejection", userId);
          return new ResourceNotFoundException("User not found with id: " + userId);
        });

    if (user.getStatus() == UserStatus.PENDING) {
      user.setStatus(UserStatus.REJECTED);
      userRepository.save(user);
      log.info("User {} status set to REJECTED", userId);

    } else {
      log.warn("User {} is not in PENDING status, cannot reject. Current status: {}", userId, user.getStatus());
      // Optionally throw an exception or just ignore
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findAllUsers() {
    log.info("Finding all users");
    return userRepository.findAll();
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    log.info("Attempting to delete user with ID: {}", userId);
    if (!userRepository.existsById(userId)) {
      log.error("User not found with ID: {} for deletion", userId);
      throw new ResourceNotFoundException("User not found with id: " + userId);
    }
    // Consider consequences of ON DELETE constraints before deleting
    userRepository.deleteById(userId);
    log.info("Deleted user with ID: {}", userId);
  }

  @Override
  @Transactional(readOnly = true)
  public User findUserById(Long userId) {
    log.info("Finding user by ID: {}", userId);
    return userRepository.findById(userId)
        .orElseThrow(() -> {
          log.error("User not found with ID: {}", userId);
          return new ResourceNotFoundException("User not found with id: " + userId);
        });
  }

  @Override
  @Transactional
  public void updateUser(User userToUpdate) {
    log.info("Attempting to update user with ID: {}", userToUpdate.getId());
    // Fetch the existing user to ensure it exists and to manage persistent state
    User existingUser = userRepository.findById(userToUpdate.getId())
        .orElseThrow(() -> {
          log.error("User not found with ID: {} during update attempt", userToUpdate.getId());
          return new ResourceNotFoundException("User not found with id: " + userToUpdate.getId());
        });

    // Update only allowed fields (e.g., status, maybe city, username?)
    // Be careful not to overwrite password unintentionally or change role easily
    existingUser.setUsername(userToUpdate.getUsername());
    existingUser.setEmail(userToUpdate.getEmail()); // Add validation for uniqueness if needed
    existingUser.setPhone(userToUpdate.getPhone()); // Add validation for uniqueness if needed
    existingUser.setCity(userToUpdate.getCity());
    existingUser.setGender(userToUpdate.getGender());
    existingUser.setStatus(userToUpdate.getStatus());
    // DO NOT update role or password here unless explicitly intended and handled
    // safely

    userRepository.save(existingUser);
    log.info("User {} updated successfully.", existingUser.getId());
  }
}
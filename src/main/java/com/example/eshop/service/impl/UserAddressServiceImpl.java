package com.example.eshop.service.impl;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.User;
import com.example.eshop.model.UserAddress;
import com.example.eshop.repository.UserAddressRepository;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserAddressServiceImpl implements UserAddressService {

  private final UserAddressRepository userAddressRepository;
  private final UserRepository userRepository;

  @Autowired
  public UserAddressServiceImpl(UserAddressRepository userAddressRepository, UserRepository userRepository) {
    this.userAddressRepository = userAddressRepository;
    this.userRepository = userRepository;
  }

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserAddress> getCurrentUserAddresses() {
    User currentUser = getCurrentUser();
    return userAddressRepository.findByUserOrderByDefaultAddressDescCreatedAtDesc(currentUser);
  }

  @Override
  @Transactional(readOnly = true)
  public UserAddress getAddressById(Long id) {
    User currentUser = getCurrentUser();
    return userAddressRepository.findByUserAndId(currentUser, id)
        .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
  }

  @Override
  @Transactional
  public UserAddress addAddress(UserAddress address) {
    User currentUser = getCurrentUser();
    address.setUser(currentUser);

    if (address.isDefault()) {
      userAddressRepository.unsetOtherDefaultAddresses(currentUser, null);
    } else if (!userAddressRepository.existsByUserAndDefaultAddressTrue(currentUser)) {
      address.setDefault(true);
    }

    return userAddressRepository.save(address);
  }

  @Override
  @Transactional
  public UserAddress updateAddress(UserAddress address) {
    User currentUser = getCurrentUser();
    UserAddress existingAddress = userAddressRepository.findByUserAndId(currentUser, address.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    existingAddress.setReceiverName(address.getReceiverName());
    existingAddress.setReceiverPhone(address.getReceiverPhone());
    existingAddress.setShippingProvince(address.getShippingProvince());
    existingAddress.setShippingCity(address.getShippingCity());
    existingAddress.setShippingDistrict(address.getShippingDistrict());
    existingAddress.setDetailedAddress(address.getDetailedAddress());

    if (address.isDefault() && !existingAddress.isDefault()) {
      userAddressRepository.unsetOtherDefaultAddresses(currentUser, address.getId());
      existingAddress.setDefault(true);
    } else if (!address.isDefault() && existingAddress.isDefault()) {
      long defaultAddressCount = userAddressRepository.findByUserOrderByDefaultAddressDescCreatedAtDesc(currentUser)
          .stream()
          .filter(a -> !a.getId().equals(address.getId()) && a.isDefault())
          .count();

      if (defaultAddressCount > 0) {
        existingAddress.setDefault(false);
      }
    }

    return userAddressRepository.save(existingAddress);
  }

  @Override
  @Transactional
  public void deleteAddress(Long addressId) {
    User currentUser = getCurrentUser();
    userAddressRepository.deleteByUserAndId(currentUser, addressId);
  }

  @Override
  @Transactional
  public void setDefaultAddress(Long addressId) {
    User currentUser = getCurrentUser();
    UserAddress address = userAddressRepository.findByUserAndId(currentUser, addressId)
        .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    if (!address.isDefault()) {
      userAddressRepository.unsetOtherDefaultAddresses(currentUser, addressId);
      address.setDefault(true);
      userAddressRepository.save(address);
    }
  }
}
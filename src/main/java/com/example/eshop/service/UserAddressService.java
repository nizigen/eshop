package com.example.eshop.service;

import com.example.eshop.model.UserAddress;
import java.util.List;

public interface UserAddressService {
  List<UserAddress> getCurrentUserAddresses();

  UserAddress getAddressById(Long id);

  UserAddress addAddress(UserAddress address);

  UserAddress updateAddress(UserAddress address);

  void deleteAddress(Long addressId);

  void setDefaultAddress(Long addressId);
}
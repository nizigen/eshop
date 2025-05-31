package com.example.eshop.service.impl;

import com.example.eshop.model.Seller;
import com.example.eshop.model.SellerDetails;
import com.example.eshop.repository.SellerDetailsRepository;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.repository.SellerFeeRateRepository;
import com.example.eshop.service.SellerService;
import com.example.eshop.dto.SellerDTO;
import com.example.eshop.enums.SellerStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {

  private final UserRepository userRepository;
  private final SellerDetailsRepository sellerDetailsRepository;
  private final SellerFeeRateRepository sellerFeeRateRepository;

  @Autowired
  public SellerServiceImpl(UserRepository userRepository,
      SellerDetailsRepository sellerDetailsRepository,
      SellerFeeRateRepository sellerFeeRateRepository) {
    this.userRepository = userRepository;
    this.sellerDetailsRepository = sellerDetailsRepository;
    this.sellerFeeRateRepository = sellerFeeRateRepository;
  }

  @Override
  public Optional<Seller> findById(Long id) {
    return userRepository.findById(id)
        .filter(user -> user instanceof Seller)
        .map(user -> (Seller) user);
  }

  @Override
  public List<Seller> findAllSellers() {
    return userRepository.findAll().stream()
        .filter(user -> user instanceof Seller)
        .map(user -> (Seller) user)
        .collect(Collectors.toList());
  }

  @Override
  public Seller saveSeller(Seller seller) {
    return (Seller) userRepository.save(seller);
  }

  @Override
  public void deleteSeller(Long id) {
    userRepository.deleteById(id);
  }

  @Override
  public void updateStoreName(Long sellerId, String storeName) {
    findSellerDetails(sellerId).ifPresent(details -> {
      details.setStoreName(storeName);
      sellerDetailsRepository.save(details);
    });
  }

  @Override
  public void updateStoreDescription(Long sellerId, String storeDescription) {
    findSellerDetails(sellerId).ifPresent(details -> {
      details.setStoreDescription(storeDescription);
      sellerDetailsRepository.save(details);
    });
  }

  @Override
  public void updateBusinessLicense(Long sellerId, String businessLicenseUrl) {
    findSellerDetails(sellerId).ifPresent(details -> {
      details.setBusinessLicenseUrl(businessLicenseUrl);
      sellerDetailsRepository.save(details);
    });
  }

  @Override
  public Optional<SellerDetails> findSellerDetails(Long sellerId) {
    return sellerDetailsRepository.findById(sellerId);
  }

  @Override
  public SellerDetails updateSellerDetails(Long sellerId, SellerDetails details) {
    if (!sellerId.equals(details.getUserId())) {
      throw new IllegalArgumentException("Seller ID mismatch");
    }
    return sellerDetailsRepository.save(details);
  }

  @Override
  public double getAverageServiceRating(Long sellerId) {
    return findSellerDetails(sellerId)
        .map(details -> details.getServiceRating().doubleValue())
        .orElse(5.0); // Default rating
  }

  @Override
  public double getBuyerPositiveRatingPercent(Long sellerId) {
    return findSellerDetails(sellerId)
        .map(details -> details.getBuyerPositiveRatingPercent().doubleValue())
        .orElse(100.0); // Default rating
  }

  @Override
  public int getSellerLevel(Long sellerId) {
    return findSellerDetails(sellerId)
        .map(SellerDetails::getSellerLevel)
        .orElse(5); // Default level (lowest level)
  }

  @Override
  public List<Seller> findSellersByStoreName(String storeName) {
    return sellerDetailsRepository.findByStoreNameContaining(storeName).stream()
        .map(SellerDetails::getUser)
        .filter(user -> user instanceof Seller)
        .map(user -> (Seller) user)
        .collect(Collectors.toList());
  }

  @Override
  public List<Seller> findSellersByLevel(int level) {
    return sellerDetailsRepository.findBySellerLevel(level).stream()
        .map(SellerDetails::getUser)
        .filter(user -> user instanceof Seller)
        .map(user -> (Seller) user)
        .collect(Collectors.toList());
  }

  @Override
  public List<Seller> findTopRatedSellers(int limit) {
    return sellerDetailsRepository.findTopByServiceRatingOrderByServiceRatingDesc(limit).stream()
        .map(SellerDetails::getUser)
        .filter(user -> user instanceof Seller)
        .map(user -> (Seller) user)
        .collect(Collectors.toList());
  }

  @Override
  public List<SellerDTO> getAllSellers() {
    return findAllSellers().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public void updateSellerStatus(Long sellerId, SellerStatus status) {
    Seller seller = findById(sellerId)
        .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));
    seller.setSellerStatus(status);
    userRepository.save(seller);
  }

  @Override
  public void updateSellerLevel(Long sellerId, Integer level) {
    if (level < 1 || level > 5) {
      throw new IllegalArgumentException("Seller level must be between 1 (highest) and 5 (lowest)");
    }
    Seller seller = findById(sellerId)
        .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));
    seller.setLevel(level);
    userRepository.save(seller);
  }

  private SellerDTO convertToDTO(Seller seller) {
    SellerDTO dto = new SellerDTO();
    dto.setId(seller.getId());
    dto.setUsername(seller.getUsername());
    dto.setEmail(seller.getEmail());
    dto.setPhone(seller.getPhone());
    dto.setStoreName(seller.getStoreName());
    dto.setStoreDescription(seller.getStoreDescription());
    dto.setBusinessLicenseUrl(seller.getBusinessLicenseUrl());
    dto.setSellerLevel(seller.getLevel());
    dto.setSellerStatus(seller.getSellerStatus());
    dto.setServiceRating(seller.getServiceRating() != null ? seller.getServiceRating().doubleValue() : null);
    dto.setBuyerPositiveRatingPercent(
        seller.getBuyerPositiveRatingPercent() != null ? seller.getBuyerPositiveRatingPercent().doubleValue() : null);
    dto.setCity(seller.getCity());
    dto.setGender(seller.getGender() != null ? seller.getGender().toString() : null);
    return dto;
  }
}
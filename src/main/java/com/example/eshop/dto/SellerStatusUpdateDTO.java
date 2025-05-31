package com.example.eshop.dto;

import com.example.eshop.enums.SellerStatus;
import lombok.Data;

@Data
public class SellerStatusUpdateDTO {
  private SellerStatus status;
}
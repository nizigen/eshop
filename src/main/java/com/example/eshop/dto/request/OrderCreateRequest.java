package com.example.eshop.dto.request;

import lombok.Data;

@Data
public class OrderCreateRequest {
  private Long productId; // 直接购买时的商品ID
  private Integer quantity; // 直接购买时的商品数量
  private boolean usePoints; // 是否使用积分
  private String shippingType; // 配送方式：online/offline
  private String offlineLocation; // 线下交易地点
  private String offlineTime; // 线下交易时间
}
package com.example.eshop.model;

public enum OrderStatus {
  PENDING_PAYMENT("待付款"),
  PENDING_DELIVERY("待发货"),
  IN_TRANSIT("运输中"),
  DELIVERED("已送达"),
  COMPLETED("交易完成"),
  CANCELED("已取消"),
  RETURN_REQUESTED("申请退货"),
  RETURN_APPROVED("退货通过"),
  RETURN_REJECTED("退货拒绝"),
  RETURN_COMPLETED("退货完成"),
  AUTO_COMPLETED("系统自动完成");

  private final String description;

  OrderStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
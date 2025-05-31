package com.example.eshop.model;

public enum ProductStatus {
  PENDING_APPROVAL("待审核"),
  ACTIVE("在售"),
  LOCKED("已锁定"),
  SOLD_OUT("已售罄"),
  UNLISTED("已下架"),
  REJECTED("已驳回");

  private final String displayName;

  ProductStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
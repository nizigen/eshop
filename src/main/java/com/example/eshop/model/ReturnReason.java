package com.example.eshop.model;

public enum ReturnReason {
  QUALITY_ISSUE("商品质量问题"),
  WRONG_ITEM("收到错误商品"),
  DAMAGED("商品损坏"),
  NOT_AS_DESCRIBED("商品与描述不符"),
  OTHER("其他原因");

  private final String description;

  ReturnReason(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
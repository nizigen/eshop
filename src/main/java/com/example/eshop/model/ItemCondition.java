package com.example.eshop.model;

public enum ItemCondition {
  NEW("全新", "success"),
  LIKE_NEW("几乎全新", "info"),
  VERY_GOOD("非常好", "primary"),
  GOOD("良好", "secondary"),
  ACCEPTABLE("可接受", "warning");

  private final String displayName;
  private final String color;

  ItemCondition(String displayName, String color) {
    this.displayName = displayName;
    this.color = color;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getColor() {
    return color;
  }
}
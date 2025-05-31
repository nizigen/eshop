package com.example.eshop.model;

public enum TransactionType {
  RECHARGE,
  PURCHASE,
  SALE_INCOME,
  WITHDRAWAL,
  PLATFORM_FEE,
  REFUND_OUT, // Refund given to buyer
  REFUND_IN, // Refund received from seller/platform
  POINTS_DEDUCTION,
  EXPENSE,
  WITHDRAW
}
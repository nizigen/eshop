package com.example.eshop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

public class MoneyUtils {
  private static final Currency DEFAULT_CURRENCY = Currency.getInstance("CNY");
  private static final int DEFAULT_SCALE = 2;
  private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
  private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

  public static BigDecimal round(BigDecimal amount) {
    return amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
  }

  public static BigDecimal round(BigDecimal amount, int scale) {
    return amount.setScale(scale, DEFAULT_ROUNDING);
  }

  public static String format(BigDecimal amount) {
    return DEFAULT_CURRENCY.getSymbol() + " " + MONEY_FORMAT.format(round(amount));
  }

  public static String format(BigDecimal amount, Currency currency) {
    return currency.getSymbol() + " " + MONEY_FORMAT.format(round(amount));
  }

  public static BigDecimal parse(String amount) {
    try {
      return new BigDecimal(amount.replaceAll("[^\\d.-]", ""));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid money amount: " + amount);
    }
  }

  public static BigDecimal calculateDiscount(BigDecimal originalPrice, BigDecimal discountPercentage) {
    BigDecimal discountFactor = BigDecimal.ONE
        .subtract(discountPercentage.divide(new BigDecimal("100"), 4, DEFAULT_ROUNDING));
    return round(originalPrice.multiply(discountFactor));
  }

  public static BigDecimal calculateTax(BigDecimal amount, BigDecimal taxRate) {
    return round(amount.multiply(taxRate.divide(new BigDecimal("100"), 4, DEFAULT_ROUNDING)));
  }

  public static BigDecimal sum(BigDecimal... amounts) {
    BigDecimal total = BigDecimal.ZERO;
    for (BigDecimal amount : amounts) {
      if (amount != null) {
        total = total.add(amount);
      }
    }
    return round(total);
  }

  public static boolean isPositive(BigDecimal amount) {
    return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
  }

  public static boolean isNegative(BigDecimal amount) {
    return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
  }

  public static boolean isZero(BigDecimal amount) {
    return amount != null && amount.compareTo(BigDecimal.ZERO) == 0;
  }

  public static BigDecimal max(BigDecimal amount1, BigDecimal amount2) {
    if (amount1 == null)
      return amount2;
    if (amount2 == null)
      return amount1;
    return amount1.compareTo(amount2) >= 0 ? amount1 : amount2;
  }

  public static BigDecimal min(BigDecimal amount1, BigDecimal amount2) {
    if (amount1 == null)
      return amount2;
    if (amount2 == null)
      return amount1;
    return amount1.compareTo(amount2) <= 0 ? amount1 : amount2;
  }

  public static BigDecimal abs(BigDecimal amount) {
    return amount != null ? amount.abs() : BigDecimal.ZERO;
  }
}
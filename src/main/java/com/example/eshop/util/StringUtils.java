package com.example.eshop.util;

import java.util.UUID;
import java.util.regex.Pattern;

public class StringUtils {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9. ()-]{7,25}$");
  private static final Pattern BANK_ACCOUNT_PATTERN = Pattern.compile("^[0-9]{16}$");

  public static boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  public static boolean isNotEmpty(String str) {
    return !isEmpty(str);
  }

  public static String generateUUID() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  public static boolean isValidEmail(String email) {
    return email != null && EMAIL_PATTERN.matcher(email).matches();
  }

  public static boolean isValidPhone(String phone) {
    return phone != null && PHONE_PATTERN.matcher(phone).matches();
  }

  public static boolean isValidBankAccount(String accountNumber) {
    return accountNumber != null && BANK_ACCOUNT_PATTERN.matcher(accountNumber).matches();
  }

  public static String maskEmail(String email) {
    if (isEmpty(email))
      return "";
    int atIndex = email.indexOf('@');
    if (atIndex <= 1)
      return email;
    return email.substring(0, 1) + "***" + email.substring(atIndex);
  }

  public static String maskPhone(String phone) {
    if (isEmpty(phone))
      return "";
    if (phone.length() <= 7)
      return phone;
    return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
  }

  public static String maskBankAccount(String accountNumber) {
    if (isEmpty(accountNumber))
      return "";
    if (accountNumber.length() <= 8)
      return accountNumber;
    return accountNumber.substring(0, 4) + "********" + accountNumber.substring(accountNumber.length() - 4);
  }

  public static String truncate(String str, int maxLength) {
    if (isEmpty(str) || str.length() <= maxLength)
      return str;
    return str.substring(0, maxLength - 3) + "...";
  }

  public static String capitalize(String str) {
    if (isEmpty(str))
      return str;
    return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
  }

  public static String normalizeSpace(String str) {
    if (isEmpty(str))
      return str;
    return str.trim().replaceAll("\\s+", " ");
  }
}
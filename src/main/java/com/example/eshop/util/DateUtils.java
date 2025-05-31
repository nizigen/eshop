package com.example.eshop.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
  private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

  public static String formatDateTime(LocalDateTime dateTime) {
    return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : "";
  }

  public static String formatDateTime(LocalDateTime dateTime, String pattern) {
    if (dateTime == null)
      return "";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return dateTime.format(formatter);
  }

  public static LocalDateTime parseDateTime(String dateTimeStr) {
    return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
  }

  public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return LocalDateTime.parse(dateTimeStr, formatter);
  }

  public static Date toDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  public static LocalDateTime toLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  public static boolean isExpired(LocalDateTime expiryTime) {
    return expiryTime != null && LocalDateTime.now().isAfter(expiryTime);
  }

  public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
    return dateTime.plusMinutes(minutes);
  }

  public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
    return dateTime.plusHours(hours);
  }

  public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
    return dateTime.plusDays(days);
  }

  public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
    return java.time.Duration.between(start, end).toMinutes();
  }

  public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
    return java.time.Duration.between(start, end).toHours();
  }

  public static long daysBetween(LocalDateTime start, LocalDateTime end) {
    return java.time.Duration.between(start, end).toDays();
  }
}
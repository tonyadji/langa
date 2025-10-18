package com.langa.backend.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

  private DateUtils() {}

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public static LocalDateTime fromTimestamp(String timestamp) {
    if (timestamp == null || timestamp.trim().isEmpty()) {
      throw new IllegalArgumentException("Timestamp cannot be null or empty");
    }

    // Try to parse as Unix timestamp (milliseconds)
    if (timestamp.matches("\\d{10,13}")) { // 10-13 digits for Unix timestamps
      try {
        long timestampValue = Long.parseLong(timestamp);

        // Handle both seconds and milliseconds timestamps
        if (timestamp.length() <= 10) {
          // Seconds timestamp
          return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampValue), ZoneId.systemDefault());
        } else {
          // Milliseconds timestamp
          return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampValue), ZoneId.systemDefault());
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid Unix timestamp format: " + timestamp, e);
      }
    }

    // Try to parse as formatted date string
    try {
      return LocalDateTime.parse(timestamp, FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Unable to parse timestamp. Expected Unix timestamp (milliseconds/seconds) or format 'yyyy-MM-dd'T'HH:mm:ss'. Got: " + timestamp, e);
    }
  }

  // Helper method to format LocalDateTime back to string
  public static String toFormattedString(LocalDateTime dateTime) {
    return dateTime.format(FORMATTER);
  }

  // Helper method to get Unix timestamp in milliseconds
  public static long toUnixTimestamp(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
}
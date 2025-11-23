package com.langa.backend.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Slf4j
public class DateUtils {

  private DateUtils() {}

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
  private static final Pattern UNIX_TIMESTAMP_PATTERN = Pattern.compile("\\d{10,13}");

  public static LocalDateTime fromTimestamp(String timestamp) {
    if (timestamp == null || timestamp.trim().isEmpty()) {
      throw new IllegalArgumentException("Timestamp cannot be null or empty");
    }

    if (UNIX_TIMESTAMP_PATTERN.matcher(timestamp).matches()) {
      try {
        long timestampValue = Long.parseLong(timestamp);

        if (timestamp.length() <= 10) {
          return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampValue), ZoneId.systemDefault());
        } else {
          return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampValue), ZoneId.systemDefault());
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid Unix timestamp format: " + timestamp, e);
      }
    }

    try {
      return LocalDateTime.parse(timestamp, FORMATTER);
    } catch (DateTimeParseException e1) {
        log.error("Invalid Unix timestamp format: " + timestamp, e1);
        try {
            return LocalDateTime.parse(timestamp);
        } catch (DateTimeParseException e2) {
            log.error("Invalid Unix timestamp format: " + timestamp, e2);
            throw new IllegalArgumentException("Unable to parse timestamp. Expected Unix timestamp (milliseconds/seconds) or format 'yyyy-MM-dd'T'HH:mm:ss[.SSS]'. Got: " + timestamp, e2);
        }

    }
  }

  public static String toFormattedString(LocalDateTime dateTime) {
    return dateTime.format(FORMATTER);
  }

  public static long toUnixTimestamp(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
}
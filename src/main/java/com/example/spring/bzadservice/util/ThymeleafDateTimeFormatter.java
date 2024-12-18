package com.example.spring.bzadservice.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ThymeleafDateTimeFormatter {

    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null || pattern.isEmpty()) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}
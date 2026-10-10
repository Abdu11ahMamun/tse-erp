package com.tse.erp.common;

import com.tse.erp.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.Year;

public final class ValidationUtil {

    private ValidationUtil() {}

    public static String requiredText(
            String value, String label, int min, int max) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(label + " cannot be empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min) {
            throw new BadRequestException(
                    label + " must be at least " + min + " characters");
        }
        if (trimmed.length() > max) {
            throw new BadRequestException(
                    label + " cannot exceed " + max + " characters");
        }
        return trimmed;
    }

    public static String optionalText(
            String value, String label, int max) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new BadRequestException(
                    label + " cannot exceed " + max + " characters");
        }
        return trimmed;
    }

    public static String requiredPattern(
            String value, String label, int min, int max,
            String regex, String message) {
        String trimmed = requiredText(value, label, min, max);
        if (!trimmed.matches(regex)) {
            throw new BadRequestException(message);
        }
        return trimmed;
    }

    public static String optionalPattern(
            String value, String label, int max,
            String regex, String message) {
        String trimmed = optionalText(value, label, max);
        if (trimmed != null && !trimmed.matches(regex)) {
            throw new BadRequestException(message);
        }
        return trimmed;
    }

    public static Integer requiredInt(
            Integer value, String label, int min, int max) {
        if (value == null) {
            throw new BadRequestException(label + " cannot be empty");
        }
        if (value < min || value > max) {
            throw new BadRequestException(
                    label + " must be between " + min + " and " + max);
        }
        return value;
    }

    public static BigDecimal positiveDecimal(
            BigDecimal value, String label, boolean required) {
        if (value == null) {
            if (required) {
                throw new BadRequestException(label + " cannot be empty");
            }
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    label + " must be greater than 0");
        }
        return value;
    }

    public static int validYear(Integer value, String label) {
        if (value == null) {
            throw new BadRequestException(label + " cannot be empty");
        }
        if (value < 1950 || value > Year.now().getValue()) {
            throw new BadRequestException(
                    label + " must be a valid year between 1950 and current year");
        }
        return value;
    }
}

package com.example.demo.users.internal.domain.model;

import com.example.demo.users.internal.domain.exception.InvalidEmailException;

import java.util.Locale;
import java.util.regex.Pattern;

/** Value object: un email normalizado (sin espacios laterales y en minúsculas) y con formato válido. */
public record Email(String value) {

    public static final int MAX_LENGTH = 255;

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+$");

    public Email {
        if (value == null) {
            throw new InvalidEmailException(null);
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_LENGTH || !FORMAT.matcher(normalized).matches()) {
            throw new InvalidEmailException(value);
        }
        value = normalized;
    }
}

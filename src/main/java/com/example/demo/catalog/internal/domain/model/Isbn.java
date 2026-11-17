package com.example.demo.catalog.internal.domain.model;

import com.example.demo.catalog.internal.domain.exception.InvalidIsbnException;

/** Value object del ISBN: no vacío y entre 10 y 20 caracteres (sin espacios en los extremos). */
public record Isbn(String value) {

    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 20;

    public Isbn {
        if (value == null || value.isBlank() || value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidIsbnException(value);
        }
    }
}

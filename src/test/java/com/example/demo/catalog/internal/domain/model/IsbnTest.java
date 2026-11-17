package com.example.demo.catalog.internal.domain.model;

import com.example.demo.catalog.internal.domain.exception.InvalidIsbnException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsbnTest {

    @ParameterizedTest
    @ValueSource(strings = {"1234567890", "9788423901500", "12345678901234567890"})
    void accepts_lengthsBetween10And20(String value) {
        assertEquals(value, new Isbn(value).value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123456789", "123456789012345678901", "          "})
    void rejects_blankOrOutOfRange(String value) {
        assertThrows(InvalidIsbnException.class, () -> new Isbn(value));
    }

    @Test
    void equalsByValue() {
        assertEquals(new Isbn("1234567890"), new Isbn("1234567890"));
    }
}

package com.example.demo.users.internal.domain.model;

import com.example.demo.users.internal.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"ana@x.es", "a@b", "  ANA@X.ES  ", "Ana.Perez+tag@Sub.Dominio.com"})
    void normalizesTrimAndLowercase(String raw) {
        assertEquals(raw.trim().toLowerCase(), new Email(raw).value());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "sin-arroba", "@x.es", "ana@", "a@@x.es", "a b@x.es", "ana@x .es"})
    void rejectsInvalidFormat(String raw) {
        assertThrows(InvalidEmailException.class, () -> new Email(raw));
    }

    @Test
    void acceptsMaxLengthAndRejectsOneMore() {
        String domain = "@x.es";
        String max = "a".repeat(Email.MAX_LENGTH - domain.length()) + domain;

        assertEquals(max, new Email(max).value());
        assertThrows(InvalidEmailException.class, () -> new Email("a" + max));
    }

    @Test
    void equalsByNormalizedValue() {
        assertEquals(new Email("ANA@x.es"), new Email(" ana@x.es"));
    }
}

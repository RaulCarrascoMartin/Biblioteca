package com.example.demo.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BcryptPasswordHasherTest {

    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher(new BCryptPasswordEncoder());

    @Test
    void hashIsBcryptAndNotThePlainPassword() {
        String hash = hasher.hash("Clave1234");
        assertTrue(hash.startsWith("$2"));
        assertNotEquals("Clave1234", hash);
    }

    @Test
    void sameInputGivesDifferentHashesDueToSalt() {
        assertNotEquals(hasher.hash("Clave1234"), hasher.hash("Clave1234"));
    }

    @Test
    void matchesAcceptsTheRightPasswordOnly() {
        String hash = hasher.hash("Clave1234");
        assertTrue(hasher.matches("Clave1234", hash));
        assertFalse(hasher.matches("otra", hash));
    }
}

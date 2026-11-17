package com.example.demo.config;

import com.example.demo.shared.TokenClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "clave-de-prueba-segura-de-mas-de-32-bytes-abc";
    private static final String EMAIL = "lector@test.es";

    private JwtService service;

    @BeforeEach
    void setUp() {
        service = new JwtService(SECRET, 1_800_000L, "biblioteca-api", "biblioteca-web");
    }

    @Test
    void generatedTokenCarriesEmailAndVersion() {
        String token = service.generate(EMAIL, 3);
        assertEquals(Optional.of(new TokenClaims(EMAIL, 3)), service.validate(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = service.generate(EMAIL, 0);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("a") ? "bb" : "aa");
        assertTrue(service.validate(tampered).isEmpty());
    }

    @Test
    void tokenWithTamperedPayloadIsRejected() {
        String[] parts = service.generate(EMAIL, 0).split("\\.");
        String otherPayload = service.generate("otro@test.es", 0).split("\\.")[1];
        assertTrue(service.validate(parts[0] + "." + otherPayload + "." + parts[2]).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtService expired = new JwtService(SECRET, 1L, "biblioteca-api", "biblioteca-web");
        String token = expired.generate(EMAIL, 0);
        Thread.sleep(20);
        assertTrue(expired.validate(token).isEmpty());
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        JwtService other = new JwtService(SECRET, 1_800_000L, "otro-emisor", "biblioteca-web");
        assertTrue(service.validate(other.generate(EMAIL, 0)).isEmpty());
    }

    @Test
    void tokenFromAnotherAudienceIsRejected() {
        JwtService other = new JwtService(SECRET, 1_800_000L, "biblioteca-api", "otra-audiencia");
        assertTrue(service.validate(other.generate(EMAIL, 0)).isEmpty());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtService other = new JwtService("otra-clave-distinta-de-mas-de-32-bytes-xyz", 1_800_000L,
                "biblioteca-api", "biblioteca-web");
        assertTrue(service.validate(other.generate(EMAIL, 0)).isEmpty());
    }

    @Test
    void garbageIsRejected() {
        assertTrue(service.validate("no-es-un-jwt").isEmpty());
        assertTrue(service.validate("").isEmpty());
        assertTrue(service.validate(null).isEmpty());
    }

    @Test
    void secretTooShortFails() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("corta", 1_800_000L, "biblioteca-api", "biblioteca-web"));
    }

    @Test
    void nullSecretFails() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService(null, 1_800_000L, "biblioteca-api", "biblioteca-web"));
    }
}

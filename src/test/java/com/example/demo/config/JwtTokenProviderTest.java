package com.example.demo.config;

import com.example.demo.shared.AuthenticatedPrincipal;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String SECRET = "clave-de-prueba-segura-de-mas-de-32-bytes-abc";
    private static final List<String> ROLES = List.of("ROLE_USER");

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, 900_000L, "biblioteca-api", "biblioteca-web");
    }

    @Test
    void generatedTokenCarriesUserIdAndRoles() {
        String token = provider.generate(7L, List.of("ROLE_ADMIN"));
        assertEquals(Optional.of(new AuthenticatedPrincipal(7L, List.of("ROLE_ADMIN"))), provider.validate(token));
    }

    @Test
    void subjectIsTheUserIdAndNoPersonalDataIsInThePayload() {
        String payload = new String(java.util.Base64.getUrlDecoder().decode(provider.generate(7L, ROLES).split("\\.")[1]));
        assertTrue(payload.contains("\"sub\":\"7\""));
        assertTrue(payload.contains("\"roles\":[\"ROLE_USER\"]"));
        assertTrue(!payload.contains("@"));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = provider.generate(1L, ROLES);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("a") ? "bb" : "aa");
        assertTrue(provider.validate(tampered).isEmpty());
    }

    @Test
    void tokenWithTamperedPayloadIsRejected() {
        String[] parts = provider.generate(1L, ROLES).split("\\.");
        String otherPayload = provider.generate(2L, List.of("ROLE_ADMIN")).split("\\.")[1];
        assertTrue(provider.validate(parts[0] + "." + otherPayload + "." + parts[2]).isEmpty());
    }

    @Test
    void unsignedAlgNoneTokenIsRejected() {
        String unsigned = Jwts.builder()
                .subject("1")
                .claim("roles", List.of("ROLE_ADMIN"))
                .issuer("biblioteca-api")
                .audience().add("biblioteca-web").and()
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .compact();

        assertTrue(unsigned.endsWith("."), "el token no lleva firma");
        assertTrue(provider.validate(unsigned).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtTokenProvider expired = new JwtTokenProvider(SECRET, 1L, "biblioteca-api", "biblioteca-web");
        String token = expired.generate(1L, ROLES);
        Thread.sleep(20);
        assertTrue(expired.validate(token).isEmpty());
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        JwtTokenProvider other = new JwtTokenProvider(SECRET, 900_000L, "otro-emisor", "biblioteca-web");
        assertTrue(provider.validate(other.generate(1L, ROLES)).isEmpty());
    }

    @Test
    void tokenFromAnotherAudienceIsRejected() {
        JwtTokenProvider other = new JwtTokenProvider(SECRET, 900_000L, "biblioteca-api", "otra-audiencia");
        assertTrue(provider.validate(other.generate(1L, ROLES)).isEmpty());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtTokenProvider other = new JwtTokenProvider("otra-clave-distinta-de-mas-de-32-bytes-xyz", 900_000L,
                "biblioteca-api", "biblioteca-web");
        assertTrue(provider.validate(other.generate(1L, ROLES)).isEmpty());
    }

    @Test
    void nonNumericSubjectIsRejected() {
        String token = Jwts.builder().subject("ana@test.es").claim("roles", ROLES)
                .issuer("biblioteca-api").audience().add("biblioteca-web").and()
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes())).compact();

        assertTrue(provider.validate(token).isEmpty());
    }

    @Test
    void garbageIsRejected() {
        assertTrue(provider.validate("no-es-un-jwt").isEmpty());
        assertTrue(provider.validate("").isEmpty());
        assertTrue(provider.validate(null).isEmpty());
    }

    @Test
    void secretTooShortFails() {
        assertThrows(IllegalStateException.class,
                () -> new JwtTokenProvider("corta", 900_000L, "biblioteca-api", "biblioteca-web"));
    }

    @Test
    void nullSecretFails() {
        assertThrows(IllegalStateException.class,
                () -> new JwtTokenProvider(null, 900_000L, "biblioteca-api", "biblioteca-web"));
    }
}

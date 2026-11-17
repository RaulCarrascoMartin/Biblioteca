package com.example.demo.auth.internal.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private static RefreshToken token(Instant expiresAt) {
        return RefreshToken.issue(1L, "hash", 2, expiresAt);
    }

    @Test
    void issueCreatesAnActiveTokenWithoutId() {
        RefreshToken token = token(NOW.plusSeconds(60));

        assertNull(token.id());
        assertFalse(token.isRevoked());
        assertEquals(2, token.tokenVersion());
    }

    @Test
    void nullUserIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RefreshToken.issue(null, "h", 0, NOW));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void blankHashIsRejected(String hash) {
        assertThrows(IllegalArgumentException.class, () -> RefreshToken.issue(1L, hash, 0, NOW));
    }

    @Test
    void nullExpirationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RefreshToken.issue(1L, "h", 0, null));
    }

    @Test
    void expirationBoundaryIsExclusive() {
        assertFalse(token(NOW.plusNanos(1)).isExpired(NOW));
        assertTrue(token(NOW).isExpired(NOW));
        assertTrue(token(NOW.minusNanos(1)).isExpired(NOW));
    }

    @Test
    void revokeReturnsARevokedCopy() {
        RefreshToken revoked = token(NOW.plusSeconds(60)).revoke(NOW);

        assertTrue(revoked.isRevoked());
        assertEquals(NOW, revoked.revokedAt());
    }

    @Test
    void revokingTwiceKeepsTheFirstRevocation() {
        RefreshToken revoked = token(NOW.plusSeconds(60)).revoke(NOW);

        assertSame(revoked, revoked.revoke(NOW.plusSeconds(5)));
    }
}

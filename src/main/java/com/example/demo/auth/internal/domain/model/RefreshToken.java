package com.example.demo.auth.internal.domain.model;

import java.time.Instant;

/**
 * Refresh token opaco de un usuario. Solo existe su hash (nunca el valor reutilizable). {@code tokenVersion}
 * es la versión de sesión del usuario al emitirlo: si cambia (revocar sesiones), el token deja de valer.
 */
public record RefreshToken(Long id, Long userId, String tokenHash, int tokenVersion, Instant expiresAt,
                           Instant revokedAt) {

    public RefreshToken {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash is required");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt is required");
        }
    }

    public static RefreshToken issue(Long userId, String tokenHash, int tokenVersion, Instant expiresAt) {
        return new RefreshToken(null, userId, tokenHash, tokenVersion, expiresAt, null);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    /** Devuelve el token revocado; si ya lo estaba, lo deja como está. */
    public RefreshToken revoke(Instant now) {
        return isRevoked() ? this : new RefreshToken(id, userId, tokenHash, tokenVersion, expiresAt, now);
    }
}

package com.example.demo.auth.internal.domain.repository;

import com.example.demo.auth.internal.domain.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;

/** Puerto de salida del dominio; lo implementa {@code persistence} (inversión de dependencias). */
public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken token);

    /** Revoca todos los refresh tokens activos del usuario (reuso detectado). */
    void revokeAllByUserId(Long userId, Instant now);
}

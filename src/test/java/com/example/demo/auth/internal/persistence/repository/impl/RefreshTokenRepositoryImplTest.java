package com.example.demo.auth.internal.persistence.repository.impl;

import com.example.demo.auth.internal.domain.model.RefreshToken;
import com.example.demo.auth.internal.persistence.repository.mapper.RefreshTokenPersistenceMapper;
import com.example.demo.support.MariaDbContainerTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contra MariaDB real con Flyway (se omite sin Docker). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RefreshTokenRepositoryImpl.class, RefreshTokenPersistenceMapper.class})
class RefreshTokenRepositoryImplTest extends MariaDbContainerTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired
    private RefreshTokenRepositoryImpl repository;
    @Autowired
    private EntityManager entityManager;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static RefreshToken token(Long userId, String hash) {
        return RefreshToken.issue(userId, hash, 1, NOW.plusSeconds(3600));
    }

    @Test
    void saveAssignsIdAndKeepsFields() {
        RefreshToken saved = repository.save(token(5L, "h1"));
        flushAndClear();

        assertNotNull(saved.id());
        RefreshToken reloaded = repository.findByTokenHash("h1").orElseThrow();
        assertEquals(5L, reloaded.userId());
        assertEquals(1, reloaded.tokenVersion());
        assertEquals(NOW.plusSeconds(3600), reloaded.expiresAt());
        assertFalse(reloaded.isRevoked());
    }

    @Test
    void saveWithExistingIdPersistsTheRevocation() {
        RefreshToken saved = repository.save(token(5L, "h1"));
        flushAndClear();

        repository.save(saved.revoke(NOW));
        flushAndClear();

        assertEquals(NOW, repository.findByTokenHash("h1").orElseThrow().revokedAt());
    }

    @Test
    void findByTokenHashWithoutResult() {
        assertTrue(repository.findByTokenHash("nope").isEmpty());
    }

    @Test
    void revokeAllByUserIdRevokesOnlyTheActiveTokensOfThatUser() {
        repository.save(token(5L, "a"));
        repository.save(token(5L, "b"));
        repository.save(token(6L, "c"));
        flushAndClear();

        repository.revokeAllByUserId(5L, NOW);
        flushAndClear();

        assertEquals(NOW, repository.findByTokenHash("a").orElseThrow().revokedAt());
        assertEquals(NOW, repository.findByTokenHash("b").orElseThrow().revokedAt());
        assertFalse(repository.findByTokenHash("c").orElseThrow().isRevoked());
    }
}

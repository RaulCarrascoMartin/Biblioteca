package com.example.demo.auth.internal.persistence.repository.mapper;

import com.example.demo.auth.internal.domain.model.RefreshToken;
import com.example.demo.auth.internal.persistence.dao.entity.RefreshTokenEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RefreshTokenPersistenceMapperTest {

    private static final Instant EXPIRES = Instant.parse("2026-01-02T10:00:00Z");
    private static final Instant REVOKED = Instant.parse("2026-01-01T10:00:00Z");

    private final RefreshTokenPersistenceMapper mapper = new RefreshTokenPersistenceMapper();

    @Test
    void newEntityRoundTripsToTheModelInUtc() {
        RefreshTokenEntity entity = mapper.toNewEntity(RefreshToken.issue(5L, "hash", 2, EXPIRES));

        assertEquals(LocalDateTime.of(2026, 1, 2, 10, 0), entity.getExpiresAt());
        RefreshToken model = mapper.toModel(entity);
        assertEquals(new RefreshToken(null, 5L, "hash", 2, EXPIRES, null), model);
    }

    @Test
    void toNewEntityRejectsModelsWithId() {
        RefreshToken withId = new RefreshToken(1L, 5L, "hash", 0, EXPIRES, null);

        assertThrows(IllegalArgumentException.class, () -> mapper.toNewEntity(withId));
    }

    @Test
    void updateManagedEntityCopiesOnlyTheRevocation() {
        RefreshTokenEntity entity = mapper.toNewEntity(RefreshToken.issue(5L, "hash", 2, EXPIRES));

        mapper.updateManagedEntity(RefreshToken.issue(5L, "hash", 2, EXPIRES).revoke(REVOKED), entity);

        assertEquals(REVOKED, mapper.toModel(entity).revokedAt());
        assertNull(mapper.toLocal(null));
    }
}

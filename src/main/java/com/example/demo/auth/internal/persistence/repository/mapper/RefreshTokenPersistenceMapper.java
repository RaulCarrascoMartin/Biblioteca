package com.example.demo.auth.internal.persistence.repository.mapper;

import com.example.demo.auth.internal.domain.model.RefreshToken;
import com.example.demo.auth.internal.persistence.dao.entity.RefreshTokenEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Convierte entre la entidad JPA y el modelo de dominio (fechas en UTC). Sin reglas de negocio. */
@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toModel(RefreshTokenEntity entity) {
        return new RefreshToken(entity.getId(), entity.getUserId(), entity.getTokenHash(),
                entity.getTokenVersion(), toInstant(entity.getExpiresAt()), toInstant(entity.getRevokedAt()));
    }

    /** Para modelos sin id; rechaza los que ya lo traen. */
    public RefreshTokenEntity toNewEntity(RefreshToken token) {
        if (token.id() != null) {
            throw new IllegalArgumentException("A new refresh token cannot have an id");
        }
        return new RefreshTokenEntity(token.userId(), token.tokenHash(), token.tokenVersion(),
                toLocal(token.expiresAt()), toLocal(token.revokedAt()));
    }

    /** Actualiza una entidad gestionada: solo cambia la revocación. */
    public void updateManagedEntity(RefreshToken token, RefreshTokenEntity entity) {
        entity.setRevokedAt(toLocal(token.revokedAt()));
    }

    public LocalDateTime toLocal(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(LocalDateTime local) {
        return local == null ? null : local.toInstant(ZoneOffset.UTC);
    }
}

package com.example.demo.auth.internal.persistence.repository.impl;

import com.example.demo.auth.internal.domain.model.RefreshToken;
import com.example.demo.auth.internal.domain.repository.RefreshTokenRepository;
import com.example.demo.auth.internal.persistence.dao.entity.RefreshTokenEntity;
import com.example.demo.auth.internal.persistence.dao.impl.jpa.RefreshTokenSpringDataRepository;
import com.example.demo.auth.internal.persistence.repository.mapper.RefreshTokenPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenSpringDataRepository jpa;
    private final RefreshTokenPersistenceMapper mapper;

    public RefreshTokenRepositoryImpl(RefreshTokenSpringDataRepository jpa, RefreshTokenPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(mapper::toModel);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity entity;
        if (token.id() == null) {
            entity = mapper.toNewEntity(token);
        } else {
            entity = jpa.findById(token.id())
                    .orElseThrow(() -> new IllegalStateException("Refresh token not found: " + token.id()));
            mapper.updateManagedEntity(token, entity);
        }
        return mapper.toModel(jpa.saveAndFlush(entity));
    }

    @Override
    public void revokeAllByUserId(Long userId, Instant now) {
        jpa.revokeAllActiveByUserId(userId, mapper.toLocal(now));
    }
}

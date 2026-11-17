package com.example.demo.auth.internal.persistence.dao.impl.jpa;

import com.example.demo.auth.internal.persistence.dao.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenSpringDataRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update RefreshTokenEntity t set t.revokedAt = :now where t.userId = :userId and t.revokedAt is null")
    int revokeAllActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}

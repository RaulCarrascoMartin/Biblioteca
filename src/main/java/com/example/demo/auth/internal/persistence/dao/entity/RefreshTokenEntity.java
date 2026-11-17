package com.example.demo.auth.internal.persistence.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/** Fechas en UTC como {@link LocalDateTime} (columnas DATETIME). El hash nunca se imprime. */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Setter
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public RefreshTokenEntity(Long userId, String tokenHash, int tokenVersion, LocalDateTime expiresAt,
                              LocalDateTime revokedAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.tokenVersion = tokenVersion;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshTokenEntity other = (RefreshTokenEntity) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RefreshTokenEntity{id=" + id + ", userId=" + userId + "}";
    }
}

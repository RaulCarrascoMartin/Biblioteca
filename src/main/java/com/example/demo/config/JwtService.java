package com.example.demo.config;

import com.example.demo.shared.TokenClaims;
import com.example.demo.shared.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService implements TokenProvider {

    private static final int MIN_SECRET_BYTES = 32;
    private static final String CLAIM_VERSION = "tv";

    private final SecretKey signingKey;
    private final long expirationMs;
    private final String issuer;
    private final String audience;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret debe definirse y tener al menos " + MIN_SECRET_BYTES + " bytes (use una clave aleatoria).");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    public String generate(String email, int version) {
        Date now = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_VERSION, version)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Optional<TokenClaims> validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Integer version = claims.get(CLAIM_VERSION, Integer.class);
            if (claims.getSubject() == null || version == null) {
                return Optional.empty();
            }
            // parseSignedClaims ya rechaza tokens caducados (ExpiredJwtException)
            return Optional.of(new TokenClaims(claims.getSubject(), version));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

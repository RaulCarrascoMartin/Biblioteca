package com.example.demo.auth.security;

import com.example.demo.auth.domain.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService implements TokenService {

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

    public String generarToken(String email) {
        return generarToken(email, 0);
    }

    @Override
    public String generarToken(String email, int version) {
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

    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public boolean esTokenValido(String token, String email) {
        try {
            Claims claims = extraerTodosLosClaims(token);
            return claims.getSubject().equalsIgnoreCase(email)
                    && !haExpirado(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean esTokenValido(String token, String email, int versionActual) {
        if (!esTokenValido(token, email)) {
            return false;
        }
        Integer version = extraerClaim(token, c -> c.get(CLAIM_VERSION, Integer.class));
        return version != null && version == versionActual;
    }

    private boolean haExpirado(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extraerTodosLosClaims(token));
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
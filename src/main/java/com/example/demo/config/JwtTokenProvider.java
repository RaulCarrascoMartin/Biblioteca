package com.example.demo.config;

import com.example.demo.shared.AuthenticatedPrincipal;
import com.example.demo.shared.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * JWT de acceso (HS256): {@code sub} = id del usuario, {@code roles}, {@code iss}, {@code aud}, {@code iat}
 * y {@code exp}. Solo va firmado, no cifrado: no lleva datos personales. Valida firma con la clave y el
 * algoritmo esperados (rechaza {@code alg: none}), emisor, audiencia y caducidad.
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private static final int MIN_SECRET_BYTES = 32;
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey signingKey;
    private final long expirationMs;
    private final String issuer;
    private final String audience;

    public JwtTokenProvider(
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
    public String generate(Long userId, List<String> roles) {
        Date now = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_ROLES, List.copyOf(roles))
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Optional<AuthenticatedPrincipal> validate(String token) {
        try {
            // parseSignedClaims exige firma (un token sin firma o con alg none falla) y rechaza los caducados
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getSubject() == null) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedPrincipal(Long.valueOf(claims.getSubject()), extractRoles(claims)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static List<String> extractRoles(Claims claims) {
        Object raw = claims.get(CLAIM_ROLES);
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
    }
}

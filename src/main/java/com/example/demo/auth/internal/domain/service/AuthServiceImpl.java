package com.example.demo.auth.internal.domain.service;

import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import com.example.demo.auth.internal.domain.mapper.AuthMapper;
import com.example.demo.auth.internal.domain.model.RefreshToken;
import com.example.demo.auth.internal.domain.repository.RefreshTokenRepository;
import com.example.demo.shared.InvalidCredentialsException;
import com.example.demo.shared.PasswordHasher;
import com.example.demo.shared.TokenProvider;
import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserCredentials;
import com.example.demo.users.dto.UserSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final UsersModule usersModule;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokens;
    private final AuthMapper mapper;
    private final Clock clock;
    private final Duration refreshTtl;
    private final SecureRandom random = new SecureRandom();
    /** Hash de relleno para que un email desconocido tarde lo mismo que uno conocido (sin revelar cuentas). */
    private final String dummyHash;

    public AuthServiceImpl(UsersModule usersModule,
                           PasswordHasher passwordHasher,
                           TokenProvider tokenProvider,
                           RefreshTokenRepository refreshTokens,
                           AuthMapper mapper,
                           Clock clock,
                           @Value("${jwt.refresh-expiration-ms:1209600000}") long refreshExpirationMs) {
        this.usersModule = usersModule;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokens = refreshTokens;
        this.mapper = mapper;
        this.clock = clock;
        this.refreshTtl = Duration.ofMillis(refreshExpirationMs);
        this.dummyHash = passwordHasher.hash("dummy-password-for-timing");
    }

    @Override
    public Optional<RegisteredUserDto> register(RegisterUserDto dto) {
        return usersModule.register(mapper.toNewUser(dto)).map(mapper::toRegisteredUser);
    }

    @Override
    @Transactional
    public TokenPairDto login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        Optional<UserCredentials> credentials = usersModule.getCredentials(normalizedEmail);
        String hash = credentials.map(UserCredentials::password).orElse(dummyHash);
        boolean matches = passwordHasher.matches(password, hash);
        if (credentials.isEmpty() || !matches) {
            throw new InvalidCredentialsException();
        }
        UserSummary user = usersModule.getByEmail(normalizedEmail).orElseThrow(InvalidCredentialsException::new);
        return issueTokens(user.id(), credentials.get());
    }

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public TokenPairDto refresh(String refreshToken) {
        Instant now = clock.instant();
        RefreshToken stored = refreshTokens.findByTokenHash(hash(refreshToken))
                .orElseThrow(InvalidCredentialsException::new);
        if (stored.isRevoked()) {
            // Reuso de un refresh ya rotado o cerrado: se asume robo y se cierran todas las sesiones
            refreshTokens.revokeAllByUserId(stored.userId(), now);
            throw new InvalidCredentialsException();
        }
        if (stored.isExpired(now)) {
            throw new InvalidCredentialsException();
        }
        UserSummary user = usersModule.getById(stored.userId()).orElseThrow(InvalidCredentialsException::new);
        UserCredentials credentials = usersModule.getCredentials(user.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (credentials.tokenVersion() != stored.tokenVersion()) {
            throw new InvalidCredentialsException();
        }
        refreshTokens.save(stored.revoke(now));
        return issueTokens(user.id(), credentials);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokens.findByTokenHash(hash(refreshToken))
                .filter(token -> !token.isRevoked())
                .ifPresent(token -> refreshTokens.save(token.revoke(clock.instant())));
    }

    private TokenPairDto issueTokens(Long userId, UserCredentials credentials) {
        String access = tokenProvider.generate(userId, List.of(ROLE_PREFIX + credentials.role()));
        String raw = newOpaqueToken();
        refreshTokens.save(RefreshToken.issue(userId, hash(raw), credentials.tokenVersion(),
                clock.instant().plus(refreshTtl)));
        return new TokenPairDto(access, raw);
    }

    private String newOpaqueToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 en hexadecimal: el token tiene 256 bits de entropía, no hace falta un hash lento. */
    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

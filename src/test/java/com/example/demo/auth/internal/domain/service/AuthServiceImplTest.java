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
import com.example.demo.users.dto.NewUser;
import com.example.demo.users.dto.UserCredentials;
import com.example.demo.users.dto.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final long REFRESH_MS = 3_600_000L;
    private static final UserSummary ANA = new UserSummary(5L, "Ana", "ana@x.es", "USER");

    @Mock
    private UsersModule usersModule;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokens;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        when(passwordHasher.hash(any())).thenReturn("dummy-hash");
        service = new AuthServiceImpl(usersModule, passwordHasher, tokenProvider, refreshTokens,
                Mappers.getMapper(AuthMapper.class), Clock.fixed(NOW, ZoneOffset.UTC), REFRESH_MS);
    }

    private static UserCredentials credentials(int version) {
        return new UserCredentials("ana@x.es", "stored-hash", "USER", version);
    }

    private RefreshToken savedToken() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokens).save(captor.capture());
        return captor.getValue();
    }

    // ---- register

    @Test
    void registerDelegatesToUsersModuleAndMapsTheResult() {
        when(usersModule.register(new NewUser("Ana", "ana@x.es", "Clave1234"))).thenReturn(Optional.of(ANA));

        assertEquals(Optional.of(new RegisteredUserDto(5L, "Ana", "ana@x.es", "USER")),
                service.register(new RegisterUserDto("Ana", "ana@x.es", "Clave1234")));
    }

    @Test
    void registerReturnsEmptyWhenEmailAlreadyExists() {
        when(usersModule.register(any())).thenReturn(Optional.empty());

        assertTrue(service.register(new RegisterUserDto("Ana", "ana@x.es", "Clave1234")).isEmpty());
    }

    // ---- login

    @Test
    void loginNormalizesEmailAndIssuesAnAccessJwtAndAStoredOpaqueRefresh() {
        when(usersModule.getCredentials("ana@x.es")).thenReturn(Optional.of(credentials(3)));
        when(passwordHasher.matches("Clave1234", "stored-hash")).thenReturn(true);
        when(usersModule.getByEmail("ana@x.es")).thenReturn(Optional.of(ANA));
        when(tokenProvider.generate(5L, List.of("ROLE_USER"))).thenReturn("jwt");

        TokenPairDto pair = service.login("  Ana@X.es ", "Clave1234");

        assertEquals("jwt", pair.accessToken());
        RefreshToken stored = savedToken();
        assertEquals(5L, stored.userId());
        assertEquals(3, stored.tokenVersion());
        assertEquals(NOW.plusMillis(REFRESH_MS), stored.expiresAt());
        assertEquals(AuthServiceImpl.hash(pair.refreshToken()), stored.tokenHash());
        assertNotEquals(pair.refreshToken(), stored.tokenHash());
    }

    @Test
    void eachLoginIssuesADifferentRefreshToken() {
        when(usersModule.getCredentials("ana@x.es")).thenReturn(Optional.of(credentials(0)));
        when(passwordHasher.matches(any(), any())).thenReturn(true);
        when(usersModule.getByEmail("ana@x.es")).thenReturn(Optional.of(ANA));
        when(tokenProvider.generate(any(), any())).thenReturn("jwt");

        assertNotEquals(service.login("ana@x.es", "p").refreshToken(), service.login("ana@x.es", "p").refreshToken());
    }

    @Test
    void loginWithWrongPasswordFailsWithoutIssuingTokens() {
        when(usersModule.getCredentials("ana@x.es")).thenReturn(Optional.of(credentials(0)));
        when(passwordHasher.matches("mala", "stored-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.login("ana@x.es", "mala"));

        verify(tokenProvider, never()).generate(any(), any());
        verify(refreshTokens, never()).save(any());
    }

    @Test
    void loginWithUnknownEmailStillComparesAgainstADummyHashAndFails() {
        when(usersModule.getCredentials("nadie@x.es")).thenReturn(Optional.empty());
        when(passwordHasher.matches("Clave1234", "dummy-hash")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () -> service.login("nadie@x.es", "Clave1234"));

        verify(passwordHasher).matches("Clave1234", "dummy-hash");
        verify(refreshTokens, never()).save(any());
    }

    @Test
    void loginFailsWhenTheUserDisappearsAfterCheckingCredentials() {
        when(usersModule.getCredentials("ana@x.es")).thenReturn(Optional.of(credentials(0)));
        when(passwordHasher.matches(any(), any())).thenReturn(true);
        when(usersModule.getByEmail("ana@x.es")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.login("ana@x.es", "Clave1234"));
    }

    // ---- refresh

    private RefreshToken activeToken(String raw, int version) {
        return new RefreshToken(10L, 5L, AuthServiceImpl.hash(raw), version, NOW.plusSeconds(60), null);
    }

    private void userExists(int version) {
        when(usersModule.getById(5L)).thenReturn(Optional.of(ANA));
        when(usersModule.getCredentials("ana@x.es")).thenReturn(Optional.of(credentials(version)));
    }

    @Test
    void refreshRotatesTheTokenRevokingTheOldOneAndIssuingANewPair() {
        when(refreshTokens.findByTokenHash(AuthServiceImpl.hash("old"))).thenReturn(Optional.of(activeToken("old", 1)));
        userExists(1);
        when(tokenProvider.generate(5L, List.of("ROLE_USER"))).thenReturn("jwt2");

        TokenPairDto pair = service.refresh("old");

        assertEquals("jwt2", pair.accessToken());
        assertNotEquals("old", pair.refreshToken());
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokens, org.mockito.Mockito.times(2)).save(captor.capture());
        RefreshToken revokedOld = captor.getAllValues().get(0);
        RefreshToken issuedNew = captor.getAllValues().get(1);
        assertEquals(10L, revokedOld.id());
        assertTrue(revokedOld.isRevoked());
        assertNull(issuedNew.id());
        assertFalse(issuedNew.isRevoked());
        assertEquals(AuthServiceImpl.hash(pair.refreshToken()), issuedNew.tokenHash());
    }

    @Test
    void reusingARevokedRefreshRevokesEveryActiveSessionOfTheUserAndFails() {
        RefreshToken revoked = activeToken("old", 1).revoke(NOW.minusSeconds(5));
        when(refreshTokens.findByTokenHash(AuthServiceImpl.hash("old"))).thenReturn(Optional.of(revoked));

        assertThrows(InvalidCredentialsException.class, () -> service.refresh("old"));

        verify(refreshTokens).revokeAllByUserId(5L, NOW);
        verify(tokenProvider, never()).generate(any(), any());
    }

    @Test
    void expiredRefreshFails() {
        RefreshToken expired = new RefreshToken(10L, 5L, AuthServiceImpl.hash("old"), 1, NOW, null);
        when(refreshTokens.findByTokenHash(AuthServiceImpl.hash("old"))).thenReturn(Optional.of(expired));

        assertThrows(InvalidCredentialsException.class, () -> service.refresh("old"));

        verify(refreshTokens, never()).save(any());
        verify(refreshTokens, never()).revokeAllByUserId(any(), any());
    }

    @Test
    void unknownRefreshFails() {
        when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.refresh("desconocido"));
    }

    @Test
    void refreshAfterRevokingSessionsFailsBecauseTheUserVersionChanged() {
        when(refreshTokens.findByTokenHash(AuthServiceImpl.hash("old"))).thenReturn(Optional.of(activeToken("old", 1)));
        userExists(2);

        assertThrows(InvalidCredentialsException.class, () -> service.refresh("old"));

        verify(refreshTokens, never()).save(any());
    }

    @Test
    void refreshOfADeletedUserFails() {
        when(refreshTokens.findByTokenHash(AuthServiceImpl.hash("old"))).thenReturn(Optional.of(activeToken("old", 1)));
        when(usersModule.getById(5L)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.refresh("old"));
    }

    // ---- logout

    @Test
    void logoutRevokesTheRefreshToken() {
        when(refreshTokens.findByTokenHash(AuthServiceImpl.hash("r"))).thenReturn(Optional.of(activeToken("r", 0)));

        service.logout("r");

        assertTrue(savedToken().isRevoked());
    }

    @Test
    void logoutIgnoresUnknownOrAlreadyRevokedTokens() {
        when(refreshTokens.findByTokenHash(AuthServiceImpl.hash("x"))).thenReturn(Optional.empty());
        when(refreshTokens.findByTokenHash(AuthServiceImpl.hash("y")))
                .thenReturn(Optional.of(activeToken("y", 0).revoke(NOW)));

        service.logout("x");
        service.logout("y");

        verify(refreshTokens, never()).save(any());
    }

    private static void assertNull(Object o) {
        org.junit.jupiter.api.Assertions.assertNull(o);
    }
}

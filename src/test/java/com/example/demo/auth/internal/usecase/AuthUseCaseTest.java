package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import com.example.demo.auth.internal.domain.service.AuthService;
import com.example.demo.shared.InvalidCredentialsException;
import com.example.demo.shared.Requester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private AuthService authService;

    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthUseCase(authService);
    }

    @Test
    void registerIsPublicAndReturnsWhatTheServiceReturns() {
        RegisterUserDto dto = new RegisterUserDto("Ana", "ana@x.es", "Clave1234");
        RegisteredUserDto registered = new RegisteredUserDto(1L, "Ana", "ana@x.es", "USER");
        when(authService.register(dto)).thenReturn(Optional.of(registered));

        assertEquals(Optional.of(registered), useCase.register(dto));
    }

    @Test
    void registerReturnsEmptyWhenEmailAlreadyRegistered() {
        RegisterUserDto dto = new RegisterUserDto("Ana", "ana@x.es", "Clave1234");
        when(authService.register(dto)).thenReturn(Optional.empty());

        assertTrue(useCase.register(dto).isEmpty());
    }

    @Test
    void loginIsPublicAndReturnsTheTokenPair() {
        when(authService.login("ana@x.es", "Clave1234")).thenReturn(new TokenPairDto("jwt", "refresh"));

        assertEquals(new TokenPairDto("jwt", "refresh"), useCase.login("ana@x.es", "Clave1234"));
    }

    @Test
    void loginPropagatesInvalidCredentials() {
        when(authService.login("ana@x.es", "mala")).thenThrow(new InvalidCredentialsException());

        assertThrows(InvalidCredentialsException.class, () -> useCase.login("ana@x.es", "mala"));
    }

    @Test
    void refreshIsPublicAndDelegatesToTheService() {
        when(authService.refresh("r1")).thenReturn(new TokenPairDto("jwt2", "r2"));

        assertEquals(new TokenPairDto("jwt2", "r2"), useCase.refresh("r1"));
    }

    @Test
    void logoutRevokesTheRefreshToken() {
        useCase.logout(new Requester(1L, Set.of("USER")), "r1");

        verify(authService).logout("r1");
    }
}

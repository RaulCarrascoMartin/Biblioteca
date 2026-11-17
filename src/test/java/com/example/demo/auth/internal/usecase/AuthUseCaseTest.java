package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.controller.dto.LoginDto;
import com.example.demo.auth.internal.controller.dto.RegisterDto;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.auth.internal.controller.mapper.AuthWebMapper;
import com.example.demo.auth.internal.domain.service.AuthService;
import com.example.demo.shared.InvalidCredentialsException;
import com.example.demo.shared.Requester;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private AuthService authService;

    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthUseCase(authService, Mappers.getMapper(AuthWebMapper.class));
    }

    @Test
    void registerIsPublicAndMapsToSpanishResponse() {
        when(authService.register(new UserDto("Ana", "ana@x.es", "Clave1234")))
                .thenReturn(Optional.of(new UserResponseDto(1L, "Ana", "ana@x.es", "USER")));

        Optional<RegisterResponse> response = useCase.register(new RegisterDto("Ana", "ana@x.es", "Clave1234"));

        assertEquals(Optional.of(new RegisterResponse(1L, "Ana", "ana@x.es", "USER")), response);
    }

    @Test
    void registerReturnsEmptyWhenEmailAlreadyRegistered() {
        when(authService.register(new UserDto("Ana", "ana@x.es", "Clave1234"))).thenReturn(Optional.empty());

        assertTrue(useCase.register(new RegisterDto("Ana", "ana@x.es", "Clave1234")).isEmpty());
    }

    @Test
    void loginIsPublicAndReturnsToken() {
        when(authService.login("ana@x.es", "Clave1234")).thenReturn("jwt");

        assertEquals("jwt", useCase.login(new LoginDto("ana@x.es", "Clave1234")));
    }

    @Test
    void loginPropagatesInvalidCredentials() {
        when(authService.login("ana@x.es", "mala")).thenThrow(new InvalidCredentialsException());

        assertThrows(InvalidCredentialsException.class, () -> useCase.login(new LoginDto("ana@x.es", "mala")));
    }

    @Test
    void logoutRevokesEveryToken() {
        useCase.logout(new Requester(1L, Set.of("USER")), List.of("t1", "t2"));

        verify(authService).revokeToken("t1");
        verify(authService).revokeToken("t2");
    }

    @Test
    void logoutWithoutTokensRevokesNothing() {
        useCase.logout(new Requester(1L, Set.of("USER")), List.of());

        verifyNoInteractions(authService);
    }
}

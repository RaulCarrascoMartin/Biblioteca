package com.example.demo.auth.internal.domain.service;

import com.example.demo.shared.CredentialsAuthenticator;
import com.example.demo.shared.InvalidCredentialsException;
import com.example.demo.shared.TokenProvider;
import com.example.demo.shared.TokenRevocation;
import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsersModule usersModule;
    @Mock
    private CredentialsAuthenticator authenticator;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private TokenRevocation tokenRevocation;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(usersModule, authenticator, tokenProvider, tokenRevocation);
    }

    @Test
    void registerDelegatesToUsersModule() {
        UserDto dto = new UserDto("Ana", "ana@x.es", "Clave1234");
        UserResponseDto created = new UserResponseDto(1L, "Ana", "ana@x.es", "USER");
        when(usersModule.register(dto)).thenReturn(Optional.of(created));

        assertEquals(Optional.of(created), service.register(dto));
    }

    @Test
    void registerReturnsEmptyWhenEmailAlreadyExists() {
        UserDto dto = new UserDto("Ana", "ana@x.es", "Clave1234");
        when(usersModule.register(dto)).thenReturn(Optional.empty());

        assertTrue(service.register(dto).isEmpty());
    }

    @Test
    void loginNormalizesEmailAndIssuesTokenWithCurrentVersion() {
        when(usersModule.getCredentials("ana@x.es"))
                .thenReturn(Optional.of(new UserCredentialsDto("ana@x.es", "hash", "USER", 3)));
        when(tokenProvider.generate("ana@x.es", 3)).thenReturn("jwt");

        String token = service.login("  Ana@X.es ", "Clave1234");

        assertEquals("jwt", token);
        verify(authenticator).authenticate("ana@x.es", "Clave1234");
    }

    @Test
    void loginUsesVersionZeroWhenCredentialsAreMissing() {
        when(usersModule.getCredentials("ana@x.es")).thenReturn(Optional.empty());
        when(tokenProvider.generate("ana@x.es", 0)).thenReturn("jwt");

        assertEquals("jwt", service.login("ana@x.es", "Clave1234"));
    }

    @Test
    void loginWithInvalidCredentialsDoesNotIssueToken() {
        doThrow(new InvalidCredentialsException()).when(authenticator).authenticate("ana@x.es", "mala");

        assertThrows(InvalidCredentialsException.class, () -> service.login("ana@x.es", "mala"));

        verify(tokenProvider, never()).generate(any(), org.mockito.ArgumentMatchers.anyInt());
        verifyNoInteractions(usersModule);
    }

    @Test
    void revokeTokenDelegatesToTokenRevocation() {
        service.revokeToken("abc.def.ghi");

        verify(tokenRevocation).revoke("abc.def.ghi");
    }
}

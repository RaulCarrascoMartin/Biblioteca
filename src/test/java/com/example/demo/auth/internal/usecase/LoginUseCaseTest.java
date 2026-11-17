package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import com.example.demo.auth.internal.domain.service.AuthService;
import com.example.demo.shared.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {


    @Mock
    private AuthService authService;

    @InjectMocks
    private LoginUseCase useCase;

    @Test
    void returnsTheTokenPair() {
        when(authService.login("ana@x.es", "Clave1234")).thenReturn(new TokenPairDto("jwt", "refresh"));

        assertEquals(new TokenPairDto("jwt", "refresh"), useCase.execute("ana@x.es", "Clave1234"));
    }

    @Test
    void propagatesInvalidCredentials() {
        when(authService.login("ana@x.es", "mala")).thenThrow(new InvalidCredentialsException());

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute("ana@x.es", "mala"));
    }
}

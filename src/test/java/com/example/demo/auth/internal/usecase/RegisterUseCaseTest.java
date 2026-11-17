package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.service.AuthService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    private static final RegisterUserDto DTO = new RegisterUserDto("Ana", "ana@x.es", "Clave1234");

    @Mock
    private AuthService authService;

    @InjectMocks
    private RegisterUseCase useCase;

    @Test
    void returnsWhatTheServiceReturns() {
        RegisteredUserDto registered = new RegisteredUserDto(1L, "Ana", "ana@x.es", "USER");
        when(authService.register(DTO)).thenReturn(Optional.of(registered));

        assertEquals(Optional.of(registered), useCase.execute(DTO));
    }

    @Test
    void returnsEmptyWhenEmailAlreadyRegistered() {
        when(authService.register(DTO)).thenReturn(Optional.empty());

        assertTrue(useCase.execute(DTO).isEmpty());
    }
}

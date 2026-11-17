package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import com.example.demo.auth.internal.domain.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {


    @Mock
    private AuthService authService;

    @InjectMocks
    private RefreshTokenUseCase useCase;

    @Test
    void delegatesToTheService() {
        when(authService.refresh("r1")).thenReturn(new TokenPairDto("jwt2", "r2"));

        assertEquals(new TokenPairDto("jwt2", "r2"), useCase.execute("r1"));
    }
}

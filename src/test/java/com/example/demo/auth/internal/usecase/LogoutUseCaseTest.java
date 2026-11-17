package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.service.AuthService;
import com.example.demo.shared.Requester;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {


    @Mock
    private AuthService authService;

    @InjectMocks
    private LogoutUseCase useCase;

    @Test
    void revokesTheRefreshToken() {
        useCase.execute(new Requester(1L, Set.of("USER")), "r1");

        verify(authService).logout("r1");
    }
}

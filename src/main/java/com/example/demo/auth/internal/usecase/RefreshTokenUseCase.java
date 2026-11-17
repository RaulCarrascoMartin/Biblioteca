package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import com.example.demo.auth.internal.domain.service.AuthService;
import org.springframework.stereotype.Component;

/** Rota el refresh token y devuelve un par nuevo. Flujo público. */
@Component
public class RefreshTokenUseCase {

    private final AuthService authService;

    public RefreshTokenUseCase(AuthService authService) {
        this.authService = authService;
    }

    public TokenPairDto execute(String refreshToken) {
        return authService.refresh(refreshToken);
    }
}

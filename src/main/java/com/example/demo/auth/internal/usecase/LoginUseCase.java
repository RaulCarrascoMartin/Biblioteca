package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import com.example.demo.auth.internal.domain.service.AuthService;
import org.springframework.stereotype.Component;

/** Inicia sesión y devuelve el par de tokens. Flujo público. */
@Component
public class LoginUseCase {

    private final AuthService authService;

    public LoginUseCase(AuthService authService) {
        this.authService = authService;
    }

    public TokenPairDto execute(String email, String password) {
        return authService.login(email, password);
    }
}

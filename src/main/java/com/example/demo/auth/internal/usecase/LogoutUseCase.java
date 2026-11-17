package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.service.AuthService;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

/** Cierra sesión revocando el refresh token. Política: usuario autenticado (lo exige la cadena de seguridad). */
@Component
public class LogoutUseCase {

    private final AuthService authService;

    public LogoutUseCase(AuthService authService) {
        this.authService = authService;
    }

    public void execute(Requester requester, String refreshToken) {
        authService.logout(refreshToken);
    }
}

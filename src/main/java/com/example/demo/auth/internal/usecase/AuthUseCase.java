package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import com.example.demo.auth.internal.domain.service.AuthService;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Flujos de autenticación. {@link #register}, {@link #login} y {@link #refresh} son públicos (sin
 * {@link Requester}: quien los usa aún no tiene sesión válida). {@link #logout} exige una persona autenticada.
 */
@Component
public class AuthUseCase {

    private final AuthService authService;

    public AuthUseCase(AuthService authService) {
        this.authService = authService;
    }

    /** Flujo público. Vacío si el email ya estaba registrado. */
    public Optional<RegisteredUserDto> register(RegisterUserDto dto) {
        return authService.register(dto);
    }

    /** Flujo público. */
    public TokenPairDto login(String email, String password) {
        return authService.login(email, password);
    }

    /** Flujo público: la credencial es el propio refresh token. */
    public TokenPairDto refresh(String refreshToken) {
        return authService.refresh(refreshToken);
    }

    /** Cualquier autenticado puede cerrar su sesión: revoca el refresh token recibido. */
    public void logout(Requester requester, String refreshToken) {
        authService.logout(refreshToken);
    }
}

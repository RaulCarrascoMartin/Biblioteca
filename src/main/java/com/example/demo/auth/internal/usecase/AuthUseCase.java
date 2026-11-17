package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.controller.dto.LoginDto;
import com.example.demo.auth.internal.controller.dto.RegisterDto;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.auth.internal.controller.mapper.AuthWebMapper;
import com.example.demo.auth.internal.domain.service.AuthService;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * Flujos de autenticación. {@link #register} y {@link #login} son públicos (sin {@link Requester}:
 * quien los usa aún no tiene sesión). {@link #logout} exige una persona autenticada.
 */
@Component
public class AuthUseCase {

    private final AuthService authService;
    private final AuthWebMapper mapper;

    public AuthUseCase(AuthService authService, AuthWebMapper mapper) {
        this.authService = authService;
        this.mapper = mapper;
    }

    /** Flujo público. Vacío si el email ya estaba registrado. */
    public Optional<RegisterResponse> register(RegisterDto dto) {
        return authService.register(mapper.toUserDto(dto)).map(mapper::toResponse);
    }

    /** Flujo público. @return el token de sesión */
    public String login(LoginDto dto) {
        return authService.login(dto.email(), dto.password());
    }

    /** Cualquier autenticado puede cerrar su sesión: revoca los tokens recibidos (cabecera y cookie). */
    public void logout(Requester requester, Collection<String> tokens) {
        tokens.forEach(authService::revokeToken);
    }
}

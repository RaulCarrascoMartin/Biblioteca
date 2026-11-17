package com.example.demo.auth.internal.domain.service;

import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.dto.TokenPairDto;

import java.util.Optional;

public interface AuthService {

    /** Devuelve vacío si el email ya estaba registrado (no se revela al cliente). */
    Optional<RegisteredUserDto> register(RegisterUserDto dto);

    /**
     * @return el par acceso/refresh
     * @throws com.example.demo.shared.InvalidCredentialsException si las credenciales no son válidas
     */
    TokenPairDto login(String email, String password);

    /**
     * Rota el refresh: lo revoca y emite un par nuevo. Si el token ya estaba usado (reuso) revoca todas
     * las sesiones del usuario.
     *
     * @throws com.example.demo.shared.InvalidCredentialsException si es desconocido, caducado, revocado o reutilizado
     */
    TokenPairDto refresh(String refreshToken);

    /** Revoca el refresh token (cierre de sesión). Idempotente: un token desconocido se ignora. */
    void logout(String refreshToken);
}

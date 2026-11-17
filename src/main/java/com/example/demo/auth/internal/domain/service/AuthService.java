package com.example.demo.auth.internal.domain.service;

import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;

import java.util.Optional;

public interface AuthService {

    /** Devuelve vacío si el email ya estaba registrado (no se revela al cliente). */
    Optional<UserResponseDto> register(UserDto dto);

    /** @return el token de sesión
     *  @throws com.example.demo.shared.InvalidCredentialsException si las credenciales no son válidas */
    String login(String email, String password);

    /** Revoca el token (cierre de sesión). */
    void revokeToken(String token);
}

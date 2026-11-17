package com.example.demo.users;

import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * API pública del módulo {@code users}. Es lo único que otros módulos pueden usar: recibe y
 * devuelve DTO, nunca el modelo interno. No aplica políticas de usuario (no recibe {@code Requester}).
 */
public interface UsersModule {

    Optional<UserResponseDto> getById(Long id);

    Optional<UserResponseDto> getByEmail(String email);

    /** Varios usuarios de una vez, indexados por id. Los ids inexistentes se omiten. */
    Map<Long, UserResponseDto> getByIds(Collection<Long> ids);

    /** Registra un usuario con rol USER. Devuelve vacío si el email ya estaba registrado. */
    Optional<UserResponseDto> register(UserDto dto);

    /** Credenciales (con el hash de la contraseña) para el módulo de autenticación. */
    Optional<UserCredentialsDto> getCredentials(String email);
}

package com.example.demo.users;

import com.example.demo.users.dto.NewUser;
import com.example.demo.users.dto.UserCredentials;
import com.example.demo.users.dto.UserSummary;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * API pública del módulo {@code users}. Es lo único que otros módulos pueden usar: recibe y
 * devuelve DTO, nunca el modelo interno. No aplica políticas de usuario (no recibe {@code Requester}).
 */
public interface UsersModule {

    Optional<UserSummary> getById(Long id);

    Optional<UserSummary> getByEmail(String email);

    /** Varios usuarios de una vez, indexados por id. Los ids inexistentes se omiten. */
    Map<Long, UserSummary> getByIds(Collection<Long> ids);

    /** Registra un usuario con rol USER. Devuelve vacío si el email ya estaba registrado. */
    Optional<UserSummary> register(NewUser dto);

    /** Credenciales (con el hash de la contraseña) para el módulo de autenticación. */
    Optional<UserCredentials> getCredentials(String email);

    /** Crea el administrador inicial si todavía no existe un usuario con ese email (idempotente). */
    void ensureAdmin(String email, String rawPassword);
}

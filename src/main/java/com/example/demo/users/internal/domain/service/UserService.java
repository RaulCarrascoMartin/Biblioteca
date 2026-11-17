package com.example.demo.users.internal.domain.service;

import com.example.demo.users.internal.domain.dto.NewUserDto;
import com.example.demo.users.internal.domain.dto.UserCredentialsDto;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    List<UserDto> getAll();

    /** @throws com.example.demo.users.internal.domain.exception.UserNotFoundException si no existe */
    UserDto getById(Long id);

    /** Búsqueda opcional: la ausencia no es un error (la usa la API pública del módulo). */
    Optional<UserDto> findById(Long id);

    Optional<UserDto> findByEmail(String email);

    /** Varios usuarios de una vez (evita N consultas), indexados por id. Los ids inexistentes se omiten. */
    Map<Long, UserDto> getByIds(Collection<Long> ids);

    /** Devuelve vacío si el email ya estaba registrado (no se revela al cliente). */
    Optional<UserDto> register(NewUserDto dto);

    Optional<UserCredentialsDto> findCredentialsByEmail(String email);

    /**
     * @throws com.example.demo.users.internal.domain.exception.UserNotFoundException si no existe
     * @throws com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException si el email es de otro
     */
    UserDto update(Long id, UserUpdateDto dto);

    /** @throws com.example.demo.users.internal.domain.exception.UserNotFoundException si no existe */
    void revokeSessions(Long id);

    /**
     * @param requesterId id de quien pide la eliminación; no puede eliminarse a sí mismo
     * @throws com.example.demo.users.internal.domain.exception.SelfDeletionException si {@code id == requesterId}
     * @throws com.example.demo.users.internal.domain.exception.UserNotFoundException si no existe
     * @throws com.example.demo.users.internal.domain.exception.UserWithLoansException si algún módulo lo usa
     */
    void delete(Long id, Long requesterId);

    /** Crea el administrador si no existe ya un usuario con ese email (idempotente). */
    void ensureAdmin(String email, String rawPassword);
}

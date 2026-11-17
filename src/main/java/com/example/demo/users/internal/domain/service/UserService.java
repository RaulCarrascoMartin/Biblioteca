package com.example.demo.users.internal.domain.service;

import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    List<UserResponseDto> getAll();

    /** @throws com.example.demo.users.internal.domain.exception.UserNotFoundException si no existe */
    UserResponseDto getById(Long id);

    /** Búsqueda opcional: la ausencia no es un error (la usa la API pública del módulo). */
    Optional<UserResponseDto> findById(Long id);

    Optional<UserResponseDto> findByEmail(String email);

    /** Varios usuarios de una vez (evita N consultas), indexados por id. Los ids inexistentes se omiten. */
    Map<Long, UserResponseDto> getByIds(Collection<Long> ids);

    /** Devuelve vacío si el email ya estaba registrado (no se revela al cliente). */
    Optional<UserResponseDto> register(UserDto dto);

    Optional<UserCredentialsDto> findCredentialsByEmail(String email);

    /**
     * @throws com.example.demo.users.internal.domain.exception.UserNotFoundException si no existe
     * @throws com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException si el email es de otro
     */
    UserResponseDto update(Long id, UserUpdateDto dto);

    /** @throws com.example.demo.users.internal.domain.exception.UserNotFoundException si no existe */
    void revokeSessions(Long id);

    /**
     * @param requesterId id de quien pide la eliminación; no puede eliminarse a sí mismo
     * @throws com.example.demo.users.internal.domain.exception.SelfDeletionException si {@code id == requesterId}
     * @throws com.example.demo.users.internal.domain.exception.UserNotFoundException si no existe
     * @throws com.example.demo.users.internal.domain.exception.UserWithLoansException si algún módulo lo usa
     */
    void delete(Long id, Long requesterId);
}

package com.example.demo.usuario.internal.domain;

import com.example.demo.usuario.dto.UsuarioCredencialesDTO;
import com.example.demo.usuario.dto.UsuarioDTO;
import com.example.demo.usuario.dto.UsuarioResponseDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UsuarioService {

    List<UsuarioResponseDTO> obtenerTodos();

    Optional<UsuarioResponseDTO> obtenerPorId(Long id);

    Optional<UsuarioResponseDTO> obtenerPorEmail(String email);

    /** Varios usuarios de una vez (evita N consultas), indexados por id. Los ids inexistentes se omiten. */
    Map<Long, UsuarioResponseDTO> obtenerPorIds(Collection<Long> ids);

    /** Devuelve vacío si el email ya estaba registrado (no se revela al cliente). */
    Optional<UsuarioResponseDTO> registrar(UsuarioDTO dto);

    Optional<UsuarioCredencialesDTO> obtenerCredenciales(String email);

    /** Devuelve vacío si el usuario no existe. */
    Optional<UsuarioResponseDTO> actualizar(Long id, UsuarioUpdateDTO usuarioNuevo);

    boolean revocarSesiones(Long id);

    /** @param emailSolicitante email de quien pide la eliminación; no puede eliminarse a sí mismo */
    void eliminar(Long id, String emailSolicitante);
}

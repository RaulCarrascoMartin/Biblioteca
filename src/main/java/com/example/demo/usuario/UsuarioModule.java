package com.example.demo.usuario;

import com.example.demo.usuario.dto.UsuarioCredencialesDTO;
import com.example.demo.usuario.dto.UsuarioDTO;
import com.example.demo.usuario.dto.UsuarioResponseDTO;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * API pública del módulo {@code usuario}. Es lo único que otros módulos pueden usar: recibe y
 * devuelve DTO, nunca el modelo interno.
 */
public interface UsuarioModule {

    Optional<UsuarioResponseDTO> obtenerPorId(Long id);

    Optional<UsuarioResponseDTO> obtenerPorEmail(String email);

    /** Varios usuarios de una vez, indexados por id. Los ids inexistentes se omiten. */
    Map<Long, UsuarioResponseDTO> obtenerPorIds(Collection<Long> ids);

    /** Registra un usuario con rol USER. Devuelve vacío si el email ya estaba registrado. */
    Optional<UsuarioResponseDTO> registrar(UsuarioDTO dto);

    /** Credenciales (con el hash de la contraseña) para el módulo de autenticación. */
    Optional<UsuarioCredencialesDTO> obtenerCredenciales(String email);
}

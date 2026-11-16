package com.example.demo.usuario.internal.domain;

import com.example.demo.usuario.dto.UsuarioCredencialesDTO;
import com.example.demo.usuario.dto.UsuarioResponseDTO;
import org.mapstruct.Mapper;

/** Convierte el modelo {@link Usuario} en DTO. Solo transforma datos, sin reglas de negocio. */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    /** Nunca expone la contraseña. */
    UsuarioResponseDTO toResponse(Usuario usuario);

    UsuarioCredencialesDTO toCredenciales(Usuario usuario);
}

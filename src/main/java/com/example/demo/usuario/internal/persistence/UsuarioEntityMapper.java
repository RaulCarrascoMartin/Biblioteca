package com.example.demo.usuario.internal.persistence;

import com.example.demo.usuario.internal.domain.Usuario;
import org.mapstruct.Mapper;

/** Convierte entre la entidad JPA y el modelo de dominio. */
@Mapper(componentModel = "spring")
public interface UsuarioEntityMapper {

    Usuario toModel(UsuarioEntity entity);

    UsuarioEntity toEntity(Usuario usuario);
}

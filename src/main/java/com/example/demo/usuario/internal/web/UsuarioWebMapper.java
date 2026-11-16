package com.example.demo.usuario.internal.web;

import com.example.demo.usuario.internal.domain.UsuarioUpdateDTO;
import org.mapstruct.Mapper;

/** Convierte el DTO web en el DTO del servicio. Solo transforma datos. */
@Mapper(componentModel = "spring")
public interface UsuarioWebMapper {

    UsuarioUpdateDTO toDto(UsuarioUpdateRequest request);
}

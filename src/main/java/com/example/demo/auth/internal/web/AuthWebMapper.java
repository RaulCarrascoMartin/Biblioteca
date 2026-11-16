package com.example.demo.auth.internal.web;

import com.example.demo.usuario.dto.UsuarioDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthWebMapper {

    UsuarioDTO toUsuarioDto(RegistroDTO registro);
}

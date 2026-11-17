package com.example.demo.users.internal.domain.mapper;

import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.domain.model.User;
import org.mapstruct.Mapper;

/** Convierte el modelo {@link User} en DTO públicos. Solo transforma datos, sin reglas de negocio. */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /** Nunca expone la contraseña. */
    UserResponseDto toResponse(User user);

    UserCredentialsDto toCredentials(User user);
}

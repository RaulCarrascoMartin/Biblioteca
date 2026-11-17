package com.example.demo.auth.internal.controller.mapper;

import com.example.demo.auth.internal.controller.dto.RegisterDto;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Convierte entre los DTO web (JSON en español) y los DTO públicos de users. Solo transforma datos. */
@Mapper(componentModel = "spring")
public interface AuthWebMapper {

    @Mapping(source = "nombre", target = "name")
    UserDto toUserDto(RegisterDto dto);

    @Mapping(source = "name", target = "nombre")
    @Mapping(source = "role", target = "rol")
    RegisterResponse toResponse(UserResponseDto dto);
}

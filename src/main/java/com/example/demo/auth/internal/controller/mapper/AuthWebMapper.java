package com.example.demo.auth.internal.controller.mapper;

import com.example.demo.auth.internal.controller.dto.LoginResponse;
import com.example.demo.auth.internal.controller.dto.RegisterRequest;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import org.mapstruct.Mapper;

/** Convierte entre los DTO web (JSON en español) y los DTO del servicio. Solo transforma datos. */
@Mapper(componentModel = "spring")
public interface AuthWebMapper {

    RegisterUserDto toDto(RegisterRequest request);

    RegisterResponse toResponse(RegisteredUserDto dto);

    LoginResponse toResponse(TokenPairDto dto);
}

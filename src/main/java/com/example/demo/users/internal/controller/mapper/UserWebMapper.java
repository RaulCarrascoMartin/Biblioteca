package com.example.demo.users.internal.controller.mapper;

import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.dto.UserUpdateRequest;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import org.mapstruct.Mapper;

import java.util.List;

/** Convierte entre los DTO web y los del servicio. Solo transforma datos. */
@Mapper(componentModel = "spring")
public interface UserWebMapper {

    UserUpdateDto toDto(UserUpdateRequest request);

    UserResponse toResponse(UserDto dto);

    List<UserResponse> toResponses(List<UserDto> dtos);
}

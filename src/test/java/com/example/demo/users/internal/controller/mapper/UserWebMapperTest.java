package com.example.demo.users.internal.controller.mapper;

import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.dto.UserUpdateRequest;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserWebMapperTest {

    private final UserWebMapper mapper = Mappers.getMapper(UserWebMapper.class);

    @Test
    void toDtoRenamesNombreToName() {
        assertEquals(new UserUpdateDto("Ana", "ana@x.es", "Nueva4567"),
                mapper.toDto(new UserUpdateRequest("Ana", "ana@x.es", "Nueva4567")));
    }

    @Test
    void toResponseKeepsHttpContractFieldNames() {
        assertEquals(new UserResponse(1L, "Ana", "ana@x.es", "ADMIN"),
                mapper.toResponse(new UserResponseDto(1L, "Ana", "ana@x.es", "ADMIN")));
    }

    @Test
    void toResponsesMapsEveryElement() {
        List<UserResponse> result = mapper.toResponses(List.of(
                new UserResponseDto(1L, "Ana", "a@x.es", "USER"),
                new UserResponseDto(2L, "Bob", "b@x.es", "USER")));

        assertEquals(List.of(1L, 2L), result.stream().map(UserResponse::id).toList());
    }
}

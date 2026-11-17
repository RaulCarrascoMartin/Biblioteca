package com.example.demo.auth.internal.controller.mapper;

import com.example.demo.auth.internal.controller.dto.RegisterDto;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthWebMapperTest {

    private final AuthWebMapper mapper = Mappers.getMapper(AuthWebMapper.class);

    @Test
    void toUserDtoMapsSpanishNameToEnglish() {
        UserDto dto = mapper.toUserDto(new RegisterDto("Ana", "ana@x.es", "Clave1234"));

        assertEquals(new UserDto("Ana", "ana@x.es", "Clave1234"), dto);
    }

    @Test
    void toResponseMapsToHistoricalSpanishFieldNames() {
        RegisterResponse response = mapper.toResponse(new UserResponseDto(7L, "Ana", "ana@x.es", "USER"));

        assertEquals(new RegisterResponse(7L, "Ana", "ana@x.es", "USER"), response);
    }
}

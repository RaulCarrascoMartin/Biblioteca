package com.example.demo.auth.internal.controller.mapper;

import com.example.demo.auth.internal.controller.dto.LoginResponse;
import com.example.demo.auth.internal.controller.dto.RegisterRequest;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthWebMapperTest {

    private final AuthWebMapper mapper = Mappers.getMapper(AuthWebMapper.class);

    @Test
    void toDtoMapsSpanishNameToEnglish() {
        RegisterUserDto dto = mapper.toDto(new RegisterRequest("Ana", "ana@x.es", "Clave1234"));

        assertEquals(new RegisterUserDto("Ana", "ana@x.es", "Clave1234"), dto);
    }

    @Test
    void toResponseMapsToHistoricalSpanishFieldNames() {
        RegisterResponse response = mapper.toResponse(new RegisteredUserDto(7L, "Ana", "ana@x.es", "USER"));

        assertEquals(new RegisterResponse(7L, "Ana", "ana@x.es", "USER"), response);
    }

    @Test
    void tokenPairMapsToLoginResponse() {
        LoginResponse response = mapper.toResponse(new TokenPairDto("jwt", "refresh"));

        assertEquals(new LoginResponse("jwt", "refresh"), response);
        assertFalse(response.toString().contains("jwt"));
    }
}

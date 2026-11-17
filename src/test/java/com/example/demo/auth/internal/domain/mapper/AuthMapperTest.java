package com.example.demo.auth.internal.domain.mapper;

import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.users.dto.NewUser;
import com.example.demo.users.dto.UserSummary;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthMapperTest {

    private final AuthMapper mapper = Mappers.getMapper(AuthMapper.class);

    @Test
    void toNewUserCopiesTheRegistrationData() {
        assertEquals(new NewUser("Ana", "ana@x.es", "Clave1234"),
                mapper.toNewUser(new RegisterUserDto("Ana", "ana@x.es", "Clave1234")));
    }

    @Test
    void toRegisteredUserCopiesThePublicUser() {
        assertEquals(new RegisteredUserDto(1L, "Ana", "ana@x.es", "USER"),
                mapper.toRegisteredUser(new UserSummary(1L, "Ana", "ana@x.es", "USER")));
    }

    @Test
    void registrationDtoDoesNotPrintThePassword() {
        assertFalse(new RegisterUserDto("Ana", "ana@x.es", "Clave1234").toString().contains("Clave1234"));
    }
}

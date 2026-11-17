package com.example.demo.users.internal.domain.mapper;

import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    private static User user() {
        User u = new User();
        u.setId(4L);
        u.setName("Ana");
        u.setEmail("ana@x.es");
        u.setPassword("hash");
        u.setRole("ADMIN");
        u.setTokenVersion(2);
        return u;
    }

    @Test
    void toResponseCopiesPublicFields() {
        assertEquals(new UserResponseDto(4L, "Ana", "ana@x.es", "ADMIN"), mapper.toResponse(user()));
    }

    @Test
    void toCredentialsIncludesHashRoleAndTokenVersion() {
        assertEquals(new UserCredentialsDto("ana@x.es", "hash", "ADMIN", 2), mapper.toCredentials(user()));
    }
}

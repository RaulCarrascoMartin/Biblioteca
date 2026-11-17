package com.example.demo.users.internal.persistence;

import com.example.demo.users.internal.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserEntityMapperTest {

    private final UserEntityMapper mapper = Mappers.getMapper(UserEntityMapper.class);

    @Test
    void roundTripKeepsEveryField() {
        User user = new User();
        user.setId(3L);
        user.setName("Ana");
        user.setEmail("ana@x.es");
        user.setPassword("hash");
        user.setRole("ADMIN");
        user.setTokenVersion(5);

        UserEntity entity = mapper.toEntity(user);
        User back = mapper.toModel(entity);

        assertEquals("Ana", entity.getName());
        assertEquals(5, entity.getTokenVersion());
        assertEquals(3L, back.getId());
        assertEquals("ana@x.es", back.getEmail());
        assertEquals("hash", back.getPassword());
        assertEquals("ADMIN", back.getRole());
        assertEquals(5, back.getTokenVersion());
    }
}

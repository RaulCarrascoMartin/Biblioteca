package com.example.demo.users.internal.domain.mapper;

import com.example.demo.users.internal.domain.dto.NewUserDto;
import com.example.demo.users.internal.domain.dto.UserCredentialsDto;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.exception.InvalidEmailException;
import com.example.demo.users.internal.domain.model.Email;
import com.example.demo.users.internal.domain.model.Role;
import com.example.demo.users.internal.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    private static User user() {
        return new User(4L, "Ana", new Email("ana@x.es"), "hash", Role.ADMIN, 2);
    }

    @Test
    void toDtoCopiesPublicFields() {
        assertEquals(new UserDto(4L, "Ana", "ana@x.es", "ADMIN"), mapper.toDto(user()));
    }

    @Test
    void toCredentialsDtoIncludesHashRoleAndTokenVersion() {
        assertEquals(new UserCredentialsDto("ana@x.es", "hash", "ADMIN", 2), mapper.toCredentialsDto(user()));
    }

    @Test
    void toModelFromNewUserNormalizesAndAssignsUserRole() {
        User model = mapper.toModel(new NewUserDto(" Ana ", " ANA@x.es ", "clave"), "HASH");

        assertNull(model.id());
        assertEquals("Ana", model.name());
        assertEquals("ana@x.es", model.email().value());
        assertEquals("HASH", model.passwordHash());
        assertEquals(Role.USER, model.role());
    }

    @Test
    void toModelChecksInvariants() {
        assertThrows(InvalidEmailException.class, () -> mapper.toModel(new NewUserDto("Ana", "mal", "x"), "H"));
    }

    @Test
    void roundTripThroughDto() {
        User original = user();

        User back = mapper.toModel(mapper.toDto(original), "hash", 2);

        assertEquals(original, back);
    }
}

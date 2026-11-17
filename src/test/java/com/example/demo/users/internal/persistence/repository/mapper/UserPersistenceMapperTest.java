package com.example.demo.users.internal.persistence.repository.mapper;

import com.example.demo.users.internal.domain.exception.InvalidEmailException;
import com.example.demo.users.internal.domain.model.Email;
import com.example.demo.users.internal.domain.model.Role;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.persistence.dao.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserPersistenceMapperTest {

    private final UserPersistenceMapper mapper = new UserPersistenceMapper();

    @Test
    void toNewEntityAndBackKeepsEveryField() {
        User user = new User(null, "Ana", new Email("ana@x.es"), "hash", Role.ADMIN, 5);

        UserEntity entity = mapper.toNewEntity(user);
        User back = mapper.toModel(entity);

        assertNull(entity.getId());
        assertEquals("Ana", entity.getName());
        assertEquals("ADMIN", entity.getRole());
        assertEquals(5, entity.getTokenVersion());
        assertEquals(user, back);
    }

    @Test
    void toNewEntityRejectsModelWithId() {
        User user = new User(3L, "Ana", new Email("ana@x.es"), "hash", Role.USER, 0);

        assertThrows(IllegalArgumentException.class, () -> mapper.toNewEntity(user));
    }

    @Test
    void updateManagedEntityCopiesModelState() {
        UserEntity entity = mapper.toNewEntity(User.newUser("Ana", new Email("ana@x.es"), "hash"));

        mapper.updateManagedEntity(new User(null, "Ana M", new Email("otra@x.es"), "nuevo", Role.USER, 2), entity);

        assertEquals("Ana M", entity.getName());
        assertEquals("otra@x.es", entity.getEmail());
        assertEquals("nuevo", entity.getPassword());
        assertEquals(2, entity.getTokenVersion());
    }

    @Test
    void toModelChecksInvariantsAgain() {
        UserEntity entity = new UserEntity("Ana", "no-es-email", "hash", "USER", 0);

        assertThrows(InvalidEmailException.class, () -> mapper.toModel(entity));
    }
}

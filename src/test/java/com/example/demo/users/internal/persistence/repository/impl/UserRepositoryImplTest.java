package com.example.demo.users.internal.persistence.repository.impl;

import com.example.demo.support.MariaDbContainerTest;
import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.model.Email;
import com.example.demo.users.internal.domain.model.Role;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.persistence.repository.mapper.UserPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contra MariaDB real con Flyway (se omite sin Docker). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserRepositoryImpl.class, UserPersistenceMapper.class})
class UserRepositoryImplTest extends MariaDbContainerTest {

    @Autowired
    private UserRepositoryImpl repository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbc;

    private static User newUser(String email) {
        return User.newUser("Ana", new Email(email), "hash");
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void saveAssignsIdAndKeepsFields() {
        User saved = repository.save(newUser("ana@x.es"));
        flushAndClear();

        assertNotNull(saved.id());
        User reloaded = repository.findById(saved.id()).orElseThrow();
        assertEquals("Ana", reloaded.name());
        assertEquals(Role.USER, reloaded.role());
        assertEquals(0, reloaded.tokenVersion());
    }

    @Test
    void saveWithExistingIdUpdatesManagedEntity() {
        User saved = repository.save(newUser("ana@x.es"));
        flushAndClear();

        repository.save(saved.rename("Ana M").changePassword("nuevo"));
        flushAndClear();

        User reloaded = repository.findById(saved.id()).orElseThrow();
        assertEquals("Ana M", reloaded.name());
        assertEquals("nuevo", reloaded.passwordHash());
        assertEquals(1, reloaded.tokenVersion());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void saveWithUnknownIdThrowsNotFound() {
        User ghost = new User(9999L, "Ana", new Email("a@x.es"), "hash", Role.USER, 0);

        assertThrows(UserNotFoundException.class, () -> repository.save(ghost));
    }

    @Test
    void findByEmailWithAndWithoutResult() {
        repository.save(newUser("ana@x.es"));
        flushAndClear();

        assertTrue(repository.findByEmail("ana@x.es").isPresent());
        assertTrue(repository.findByEmail("nadie@x.es").isEmpty());
    }

    @Test
    void findAllByIdReturnsOnlyExistingOnes() {
        Long a = repository.save(newUser("a@x.es")).id();
        repository.save(newUser("b@x.es"));
        flushAndClear();

        List<User> result = repository.findAllById(List.of(a, 9999L));

        assertEquals(1, result.size());
        assertEquals("a@x.es", result.get(0).email().value());
    }

    @Test
    void deleteByIdRemovesTheUser() {
        Long id = repository.save(newUser("ana@x.es")).id();
        flushAndClear();

        repository.deleteById(id);
        flushAndClear();

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void duplicateEmailIsTranslatedToDomainException() {
        repository.save(newUser("dup@x.es"));
        flushAndClear();

        assertThrows(EmailAlreadyRegisteredException.class, () -> repository.save(newUser("dup@x.es")));
    }

    /** D16: la tabla y las columnas históricas se mantienen. */
    @Test
    void usesTheHistoricalTableAndColumns() {
        Long id = repository.save(newUser("ana@x.es")).id();
        flushAndClear();

        Integer count = jdbc.queryForObject(
                "select count(*) from usuarios where id = ? and nombre = 'Ana' and email = 'ana@x.es' "
                        + "and rol = 'USER' and token_version = 0", Integer.class, id);

        assertEquals(1, count);
    }
}

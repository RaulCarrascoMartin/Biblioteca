package com.example.demo.users.internal.persistence;

import com.example.demo.users.internal.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import({UserRepositoryImpl.class, UserEntityMapperImpl.class})
class UserRepositoryImplTest {

    @Autowired
    private UserRepositoryImpl repository;
    @Autowired
    private JpaUserRepository jpa;
    @Autowired
    private JdbcTemplate jdbc;

    private User newUser(String email) {
        User u = new User();
        u.setName("Ana");
        u.setEmail(email);
        u.setPassword("hash");
        return u;
    }

    @Test
    void saveAssignsIdAndKeepsFields() {
        User saved = repository.save(newUser("ana@x.es"));

        assertNotNull(saved.getId());
        assertEquals("USER", saved.getRole());
        assertEquals(0, saved.getTokenVersion());
        assertEquals("Ana", repository.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    void saveWithExistingIdUpdates() {
        User saved = repository.save(newUser("ana@x.es"));
        saved.setName("Ana M");
        saved.setTokenVersion(2);

        repository.save(saved);

        User reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Ana M", reloaded.getName());
        assertEquals(2, reloaded.getTokenVersion());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByEmailWithAndWithoutResult() {
        repository.save(newUser("ana@x.es"));

        assertTrue(repository.findByEmail("ana@x.es").isPresent());
        assertTrue(repository.findByEmail("nadie@x.es").isEmpty());
    }

    @Test
    void findAllByIdReturnsOnlyExistingOnes() {
        Long a = repository.save(newUser("a@x.es")).getId();
        repository.save(newUser("b@x.es"));

        List<User> result = repository.findAllById(List.of(a, 9999L));

        assertEquals(1, result.size());
        assertEquals("a@x.es", result.get(0).getEmail());
    }

    @Test
    void deleteByIdRemovesTheUser() {
        Long id = repository.save(newUser("ana@x.es")).getId();

        repository.deleteById(id);

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void emailMustBeUnique() {
        jpa.saveAndFlush(entity("dup@x.es"));

        assertThrows(DataIntegrityViolationException.class, () -> jpa.saveAndFlush(entity("dup@x.es")));
    }

    /** D16: la tabla y las columnas históricas se mantienen. */
    @Test
    void usesTheHistoricalTableAndColumns() {
        Long id = repository.save(newUser("ana@x.es")).getId();

        Integer count = jdbc.queryForObject(
                "select count(*) from usuarios where id = ? and nombre = 'Ana' and email = 'ana@x.es' "
                        + "and rol = 'USER' and token_version = 0", Integer.class, id);

        assertEquals(1, count);
    }

    private UserEntity entity(String email) {
        UserEntity e = new UserEntity();
        e.setName("Dup");
        e.setEmail(email);
        e.setPassword("hash");
        return e;
    }
}

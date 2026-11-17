package com.example.demo.catalog.internal.persistence.repository.impl;

import com.example.demo.catalog.internal.domain.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.model.Genre;
import com.example.demo.catalog.internal.domain.model.Isbn;
import com.example.demo.catalog.internal.persistence.repository.mapper.BookPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.support.MariaDbContainerTest;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contra MariaDB real con las migraciones Flyway. Ojo: parte con los 32 libros de V2 (sin el callback de
 * test que vacía la tabla), así que los tests usan ISBN propios y cuentan en relativo.
 */
@DataJpaTest(properties = {
        "spring.flyway.locations=classpath:db/migration",
        "spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({BookRepositoryImpl.class, BookPersistenceMapper.class})
class BookRepositoryImplMariaDbTest extends MariaDbContainerTest {

    @Autowired
    private BookRepositoryImpl repository;

    @Autowired
    private EntityManager entityManager;

    private static Book book(String isbn) {
        return Book.newBook("Título " + isbn, "Autor", new Isbn(isbn), Genre.AVENTURAS);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void save_assignsId_andPersistsFields() {
        Book saved = repository.save(book("9781111111117"));
        flushAndClear();

        assertNotNull(saved.id());
        Book found = repository.findById(saved.id()).orElseThrow();
        assertEquals(new Isbn("9781111111117"), found.isbn());
        assertEquals(Genre.AVENTURAS, found.genre());
        assertTrue(found.available());
    }

    @Test
    void save_existing_updatesManagedEntity() {
        Book saved = repository.save(book("9781212121212"));
        flushAndClear();

        repository.save(saved.reserve().withDetails("Nuevo", "Otra", saved.isbn(), Genre.POESIA));
        flushAndClear();

        Book found = repository.findById(saved.id()).orElseThrow();
        assertEquals("Nuevo", found.title());
        assertEquals(Genre.POESIA, found.genre());
        assertFalse(found.available());
    }

    @Test
    void save_missingId_throwsNotFound() {
        Book ghost = new Book(99999L, "T", "A", new Isbn("9781313131313"), true, Genre.CLASICA);

        assertThrows(BookNotFoundException.class, () -> repository.save(ghost));
    }

    @Test
    void findByIsbn_findsExistingAndEmptyForUnknown() {
        repository.save(book("9782222222224"));
        flushAndClear();

        assertTrue(repository.findByIsbn("9782222222224").isPresent());
        assertTrue(repository.findByIsbn("9789999999999").isEmpty());
    }

    @Test
    void findAll_andFindAllById() {
        Book a = repository.save(book("9783333333331"));
        repository.save(book("9784444444448"));
        flushAndClear();

        int seeded = 32; // libros de V2
        assertEquals(seeded + 2, repository.findAll().size());
        List<Book> byId = repository.findAllById(List.of(a.id(), 9999L));
        assertEquals(1, byId.size());
    }

    @Test
    void findByIdWithLock_returnsBook() {
        Book saved = repository.save(book("9785555555555"));
        flushAndClear();

        assertTrue(repository.findByIdWithLock(saved.id()).isPresent());
        assertTrue(repository.findByIdWithLock(9999L).isEmpty());
    }

    @Test
    void deleteById_removesBook() {
        Book saved = repository.save(book("9786666666662"));
        flushAndClear();

        repository.deleteById(saved.id());
        flushAndClear();

        assertTrue(repository.findById(saved.id()).isEmpty());
    }

    @Test
    void save_duplicateIsbn_translatesToDomainException() {
        repository.save(book("9787777777779"));

        assertThrows(DuplicateIsbnException.class, () -> repository.save(book("9787777777779")));
    }
}

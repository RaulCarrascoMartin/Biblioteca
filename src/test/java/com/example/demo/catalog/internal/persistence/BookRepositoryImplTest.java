package com.example.demo.catalog.internal.persistence;

import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.internal.domain.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import({BookRepositoryImpl.class, BookEntityMapperImpl.class})
class BookRepositoryImplTest {

    @Autowired
    private BookRepositoryImpl repository;

    private Book book(String isbn) {
        Book b = new Book();
        b.setTitle("Título " + isbn);
        b.setAuthor("Autor");
        b.setIsbn(isbn);
        b.setGenre(Genre.AVENTURAS);
        return b;
    }

    @Test
    void save_assignsId_andPersistsFields() {
        Book saved = repository.save(book("9781111111117"));

        assertNotNull(saved.getId());
        Book found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("9781111111117", found.getIsbn());
        assertEquals(Genre.AVENTURAS, found.getGenre());
        assertTrue(found.isAvailable());
    }

    @Test
    void findByIsbn_findsExistingAndEmptyForUnknown() {
        repository.save(book("9782222222224"));

        assertTrue(repository.findByIsbn("9782222222224").isPresent());
        assertTrue(repository.findByIsbn("9789999999999").isEmpty());
    }

    @Test
    void findAll_andFindAllById() {
        Book a = repository.save(book("9783333333331"));
        repository.save(book("9784444444448"));

        assertEquals(2, repository.findAll().size());
        List<Book> byId = repository.findAllById(List.of(a.getId(), 9999L));
        assertEquals(1, byId.size());
    }

    @Test
    void findByIdWithLock_returnsBook() {
        Book saved = repository.save(book("9785555555555"));

        assertTrue(repository.findByIdWithLock(saved.getId()).isPresent());
        assertTrue(repository.findByIdWithLock(9999L).isEmpty());
    }

    @Test
    void deleteById_removesBook() {
        Book saved = repository.save(book("9786666666662"));

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void save_duplicateIsbn_violatesUniqueConstraint() {
        repository.save(book("9787777777779"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.save(book("9787777777779"));
            // fuerza el flush para que la restricción única se compruebe
            repository.findAll();
        });
    }
}

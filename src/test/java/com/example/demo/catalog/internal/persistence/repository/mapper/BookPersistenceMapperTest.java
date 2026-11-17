package com.example.demo.catalog.internal.persistence.repository.mapper;

import com.example.demo.catalog.internal.domain.exception.InvalidIsbnException;
import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.model.Genre;
import com.example.demo.catalog.internal.domain.model.Isbn;
import com.example.demo.catalog.internal.persistence.dao.entity.BookEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookPersistenceMapperTest {

    private final BookPersistenceMapper mapper = new BookPersistenceMapper();

    @Test
    void toNewEntity_thenToModel_keepsAllFields() {
        Book book = Book.newBook("T", "A", new Isbn("1234567890"), Genre.HISTORICA);

        BookEntity entity = mapper.toNewEntity(book);
        Book model = mapper.toModel(entity);

        assertNull(model.id());
        assertEquals("T", model.title());
        assertEquals("A", model.author());
        assertEquals(new Isbn("1234567890"), model.isbn());
        assertEquals(Genre.HISTORICA, model.genre());
        assertEquals(book, model);
    }

    @Test
    void toNewEntity_rejectsModelWithId() {
        Book book = new Book(4L, "T", "A", new Isbn("1234567890"), true, Genre.CLASICA);

        assertThrows(IllegalArgumentException.class, () -> mapper.toNewEntity(book));
    }

    @Test
    void updateManagedEntity_copiesStateOntoEntity() {
        BookEntity entity = new BookEntity("Old", "Old", "0000000000", true, Genre.CLASICA);

        mapper.updateManagedEntity(new Book(4L, "T", "A", new Isbn("1234567890"), false, Genre.POESIA), entity);

        assertEquals("T", entity.getTitle());
        assertEquals("A", entity.getAuthor());
        assertEquals("1234567890", entity.getIsbn());
        assertFalse(entity.isAvailable());
        assertEquals(Genre.POESIA, entity.getGenre());
    }

    @Test
    void toModel_rebuildsThroughConstructor_soInvariantsAreChecked() {
        BookEntity entity = new BookEntity("T", "A", "123", true, Genre.CLASICA);

        assertThrows(InvalidIsbnException.class, () -> mapper.toModel(entity));
    }
}

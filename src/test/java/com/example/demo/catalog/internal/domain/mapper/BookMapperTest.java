package com.example.demo.catalog.internal.domain.mapper;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.exception.InvalidBookException;
import com.example.demo.catalog.internal.domain.exception.InvalidIsbnException;
import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.model.Genre;
import com.example.demo.catalog.internal.domain.model.Isbn;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookMapperTest {

    private final BookMapper mapper = Mappers.getMapper(BookMapper.class);

    @Test
    void toDto_copiesAllFields() {
        Book book = new Book(3L, "T", "A", new Isbn("1234567890"), false, Genre.FANTASIA);

        assertEquals(new BookDto(3L, "T", "A", "1234567890", false, Genre.FANTASIA), mapper.toDto(book));
    }

    @Test
    void toModel_startsWithoutIdAndAvailable() {
        Book book = mapper.toModel(new SaveBookDto("T", "A", "1234567890", Genre.POESIA));

        assertNull(book.id());
        assertTrue(book.available());
        assertEquals(new Isbn("1234567890"), book.isbn());
        assertEquals(Genre.POESIA, book.genre());
    }

    @Test
    void toModel_invalidIsbn_throwsDomainException() {
        assertThrows(InvalidIsbnException.class, () -> mapper.toModel(new SaveBookDto("T", "A", "123", Genre.POESIA)));
    }

    @Test
    void toModel_nullGenre_throwsDomainException() {
        assertThrows(InvalidBookException.class,
                () -> mapper.toModel(new SaveBookDto("T", "A", "1234567890", null)));
    }
}

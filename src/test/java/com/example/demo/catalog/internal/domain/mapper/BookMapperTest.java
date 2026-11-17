package com.example.demo.catalog.internal.domain.mapper;

import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.model.Book;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookMapperTest {

    private final BookMapper mapper = Mappers.getMapper(BookMapper.class);

    @Test
    void toResponse_copiesAllFields() {
        Book book = new Book();
        book.setId(3L);
        book.setTitle("T");
        book.setAuthor("A");
        book.setIsbn("123");
        book.setAvailable(false);
        book.setGenre(Genre.FANTASIA);

        assertEquals(new BookResponseDto(3L, "T", "A", "123", false, Genre.FANTASIA), mapper.toResponse(book));
    }

    @Test
    void toModel_ignoresIdAndStartsAvailable() {
        Book book = mapper.toModel(new SaveBookDto("T", "A", "123", Genre.POESIA));

        assertNull(book.getId());
        assertEquals("T", book.getTitle());
        assertEquals("A", book.getAuthor());
        assertEquals("123", book.getIsbn());
        assertEquals(Genre.POESIA, book.getGenre());
        assertEquals(true, book.isAvailable());
    }

    @Test
    void update_overwritesFields_butKeepsIdAvailabilityAndNullGenre() {
        Book book = new Book();
        book.setId(7L);
        book.setAvailable(false);
        book.setGenre(Genre.CLASICA);

        mapper.update(new SaveBookDto("T2", "A2", "999", null), book);

        assertEquals(7L, book.getId());
        assertFalse(book.isAvailable());
        assertEquals("T2", book.getTitle());
        assertEquals("999", book.getIsbn());
        assertEquals(Genre.CLASICA, book.getGenre());
    }
}

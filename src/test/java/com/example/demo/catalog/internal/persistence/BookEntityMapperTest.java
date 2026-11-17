package com.example.demo.catalog.internal.persistence;

import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.internal.domain.model.Book;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BookEntityMapperTest {

    private final BookEntityMapper mapper = Mappers.getMapper(BookEntityMapper.class);

    @Test
    void roundTrip_keepsAllFields() {
        BookEntity entity = new BookEntity();
        entity.setId(4L);
        entity.setTitle("T");
        entity.setAuthor("A");
        entity.setIsbn("123");
        entity.setAvailable(false);
        entity.setGenre(Genre.HISTORICA);

        Book model = mapper.toModel(entity);
        BookEntity back = mapper.toEntity(model);

        assertEquals(4L, model.getId());
        assertEquals("T", model.getTitle());
        assertFalse(model.isAvailable());
        assertEquals(Genre.HISTORICA, model.getGenre());
        assertEquals(entity, back);
        assertEquals("A", back.getAuthor());
        assertEquals("123", back.getIsbn());
    }
}

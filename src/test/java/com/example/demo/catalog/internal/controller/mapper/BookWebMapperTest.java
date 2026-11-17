package com.example.demo.catalog.internal.controller.mapper;

import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookWebMapperTest {

    private final BookWebMapper mapper = Mappers.getMapper(BookWebMapper.class);

    @Test
    void toDto_copiesRequestFields_mappingPublicGenreToDomain() {
        assertEquals(new SaveBookDto("T", "A", "1234567890",
                        com.example.demo.catalog.internal.domain.model.Genre.MISTERIO),
                mapper.toDto(new BookRequest("T", "A", "1234567890", Genre.MISTERIO)));
    }

    @Test
    void toDto_nullGenreStaysNull() {
        assertNull(mapper.toDto(new BookRequest("T", "A", "1234567890", null)).genre());
    }

    @Test
    void toResponse_copiesDtoFields_mappingDomainGenreToPublic() {
        assertEquals(new BookResponse(1L, "T", "A", "123", true, Genre.POESIA),
                mapper.toResponse(new BookDto(1L, "T", "A", "123", true,
                        com.example.demo.catalog.internal.domain.model.Genre.POESIA)));
    }

    @Test
    void toResponses_mapsEveryElement() {
        List<BookResponse> result = mapper.toResponses(List.of(
                new BookDto(1L, "T", "A", "1", true, null),
                new BookDto(2L, "U", "B", "2", false, null)));

        assertEquals(2, result.size());
        assertEquals(2L, result.get(1).id());
    }
}

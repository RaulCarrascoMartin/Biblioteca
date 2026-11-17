package com.example.demo.loans.internal.controller.mapper;

import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.loans.internal.controller.dto.LoanBookResponse;
import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanWebMapperTest {

    private final LoanWebMapper mapper = Mappers.getMapper(LoanWebMapper.class);

    private static LoanDto dto(Long id) {
        return new LoanDto(id, LocalDate.of(2026, 1, 2), null,
                new BookSummary(3L, "T", "A", "123", false, Genre.POESIA), 4L, "Ana", "ana@x.es");
    }

    @Test
    void toResponse_copiesEveryFieldIncludingTheNestedBook() {
        LoanResponse expected = new LoanResponse(1L, LocalDate.of(2026, 1, 2), null,
                new LoanBookResponse(3L, "T", "A", "123", false, Genre.POESIA), 4L, "Ana", "ana@x.es");

        assertEquals(expected, mapper.toResponse(dto(1L)));
    }

    @Test
    void toResponses_mapsEveryElement() {
        List<LoanResponse> result = mapper.toResponses(List.of(dto(1L), dto(2L)));

        assertEquals(2, result.size());
        assertEquals(2L, result.get(1).id());
    }
}

package com.example.demo.loans.internal.domain.mapper;

import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.users.dto.UserSummary;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanMapperTest {

    private final LoanMapper mapper = Mappers.getMapper(LoanMapper.class);

    @Test
    void toDto_composesLoanBookAndUser() {
        Loan loan = new Loan(1L, 2L, 3L, LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 9));
        BookSummary book = new BookSummary(2L, "T", "A", "123", true, Genre.POESIA);
        UserSummary user = new UserSummary(3L, "Ana", "ana@x.es", "USER");

        LoanDto dto = mapper.toDto(loan, book, user);

        assertEquals(new LoanDto(1L, LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 9),
                book, 3L, "Ana", "ana@x.es"), dto);
    }

}

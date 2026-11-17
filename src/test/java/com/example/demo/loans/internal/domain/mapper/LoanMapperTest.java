package com.example.demo.loans.internal.domain.mapper;

import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.loans.dto.LoanResponseDto;
import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.users.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoanMapperTest {

    private final LoanMapper mapper = Mappers.getMapper(LoanMapper.class);

    @Test
    void toResponse_composesLoanBookAndUser() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setBookId(2L);
        loan.setUserId(3L);
        loan.setLoanDate(LocalDate.of(2026, 1, 2));
        loan.setReturnDate(LocalDate.of(2026, 1, 9));
        BookResponseDto book = new BookResponseDto(2L, "T", "A", "123", true, Genre.POESIA);
        UserResponseDto user = new UserResponseDto(3L, "Ana", "ana@x.es", "USER");

        LoanResponseDto dto = mapper.toResponse(loan, book, user);

        assertEquals(new LoanResponseDto(1L, LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 9),
                book, 3L, "Ana", "ana@x.es"), dto);
    }

    @Test
    void toModel_copiesIdsAndLeavesIdAndDatesUnset() {
        Loan loan = mapper.toModel(new CreateLoanDto(2L, 3L));

        assertEquals(2L, loan.getBookId());
        assertEquals(3L, loan.getUserId());
        assertNull(loan.getId());
        assertNull(loan.getLoanDate());
        assertNull(loan.getReturnDate());
    }
}

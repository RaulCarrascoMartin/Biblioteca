package com.example.demo.loans.internal.persistence;

import com.example.demo.loans.internal.domain.model.Loan;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanEntityMapperTest {

    private final LoanEntityMapper mapper = Mappers.getMapper(LoanEntityMapper.class);

    @Test
    void roundTrip_keepsAllFields() {
        LoanEntity entity = new LoanEntity();
        entity.setId(4L);
        entity.setBookId(5L);
        entity.setUserId(6L);
        entity.setLoanDate(LocalDate.of(2026, 1, 2));
        entity.setReturnDate(LocalDate.of(2026, 1, 9));

        Loan model = mapper.toModel(entity);
        LoanEntity back = mapper.toEntity(model);

        assertEquals(4L, model.getId());
        assertEquals(5L, model.getBookId());
        assertEquals(6L, model.getUserId());
        assertEquals(LocalDate.of(2026, 1, 2), model.getLoanDate());
        assertEquals(LocalDate.of(2026, 1, 9), model.getReturnDate());
        assertEquals(entity, back);
        assertEquals(5L, back.getBookId());
        assertEquals(LocalDate.of(2026, 1, 9), back.getReturnDate());
    }
}

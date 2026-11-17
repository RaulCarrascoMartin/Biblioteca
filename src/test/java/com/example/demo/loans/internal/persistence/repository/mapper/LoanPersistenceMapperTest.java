package com.example.demo.loans.internal.persistence.repository.mapper;

import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.loans.internal.persistence.dao.entity.LoanEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanPersistenceMapperTest {

    private final LoanPersistenceMapper mapper = new LoanPersistenceMapper();

    @Test
    void toNewEntity_copiesFieldsWithoutId() {
        Loan loan = new Loan(null, 5L, 6L, LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 9));

        LoanEntity entity = mapper.toNewEntity(loan);

        assertNull(entity.getId());
        assertEquals(5L, entity.getBookId());
        assertEquals(6L, entity.getUserId());
        assertEquals(LocalDate.of(2026, 1, 2), entity.getLoanDate());
        assertEquals(LocalDate.of(2026, 1, 9), entity.getReturnDate());
    }

    @Test
    void toNewEntity_rejectsAModelWithId() {
        Loan loan = new Loan(1L, 5L, 6L, LocalDate.of(2026, 1, 2), null);

        assertThrows(IllegalArgumentException.class, () -> mapper.toNewEntity(loan));
    }

    @Test
    void updateManagedEntity_copiesTheReturnDate() {
        LoanEntity entity = mapper.toNewEntity(new Loan(null, 5L, 6L, LocalDate.of(2026, 1, 2), null));

        mapper.updateManagedEntity(new Loan(null, 5L, 6L, LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 9)), entity);

        assertEquals(LocalDate.of(2026, 1, 9), entity.getReturnDate());
    }

    @Test
    void toModel_rebuildsTheLoan() {
        LoanEntity entity = mapper.toNewEntity(new Loan(null, 5L, 6L, LocalDate.of(2026, 1, 2), null));

        Loan model = mapper.toModel(entity);

        assertEquals(new Loan(null, 5L, 6L, LocalDate.of(2026, 1, 2), null), model);
    }
}

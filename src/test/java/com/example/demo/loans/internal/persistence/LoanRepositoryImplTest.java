package com.example.demo.loans.internal.persistence;

import com.example.demo.loans.internal.domain.model.Loan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import({LoanRepositoryImpl.class, LoanEntityMapperImpl.class})
class LoanRepositoryImplTest {

    @Autowired
    private LoanRepositoryImpl repository;
    @Autowired
    private JdbcTemplate jdbc;

    private Loan newLoan(Long bookId, Long userId) {
        Loan loan = new Loan();
        loan.setBookId(bookId);
        loan.setUserId(userId);
        loan.setLoanDate(LocalDate.of(2026, 1, 2));
        return loan;
    }

    @Test
    void saveAssignsIdAndKeepsFields() {
        Loan saved = repository.save(newLoan(10L, 20L));

        assertNotNull(saved.getId());
        Loan reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(10L, reloaded.getBookId());
        assertEquals(20L, reloaded.getUserId());
        assertEquals(LocalDate.of(2026, 1, 2), reloaded.getLoanDate());
        assertNull(reloaded.getReturnDate());
    }

    @Test
    void saveWithExistingIdUpdatesTheReturnDate() {
        Loan saved = repository.save(newLoan(10L, 20L));
        saved.setReturnDate(LocalDate.of(2026, 1, 9));

        repository.save(saved);

        assertEquals(LocalDate.of(2026, 1, 9), repository.findById(saved.getId()).orElseThrow().getReturnDate());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByIdWithoutResultIsEmpty() {
        assertTrue(repository.findById(9999L).isEmpty());
    }

    @Test
    void findByUserIdReturnsOnlyThatUsersLoans() {
        repository.save(newLoan(10L, 20L));
        repository.save(newLoan(11L, 20L));
        repository.save(newLoan(12L, 21L));

        List<Loan> result = repository.findByUserId(20L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.getUserId().equals(20L)));
        assertTrue(repository.findByUserId(99L).isEmpty());
    }

    @Test
    void existsByUserIdAndByBookId() {
        repository.save(newLoan(10L, 20L));

        assertTrue(repository.existsByUserId(20L));
        assertFalse(repository.existsByUserId(21L));
        assertTrue(repository.existsByBookId(10L));
        assertFalse(repository.existsByBookId(11L));
    }

    /** D16: la tabla y las columnas históricas se mantienen. */
    @Test
    void usesTheHistoricalTableAndColumns() {
        Loan loan = newLoan(10L, 20L);
        loan.setReturnDate(LocalDate.of(2026, 1, 9));
        Long id = repository.save(loan).getId();

        Integer count = jdbc.queryForObject(
                "select count(*) from prestamos where id = ? and libro_id = 10 and usuario_id = 20 "
                        + "and fecha_prestamo = '2026-01-02' and fecha_devolucion = '2026-01-09'",
                Integer.class, id);

        assertEquals(1, count);
    }
}

package com.example.demo.loans.internal.persistence.repository.impl;

import com.example.demo.loans.internal.domain.exception.LoanConcurrentModificationException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.domain.model.Loan;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Casos de {@link LoanRepositoryImpl}; los ejecutan una clase sobre MariaDB y otra sobre H2. */
@Transactional
interface LoanRepositoryContract {

    LocalDate DAY = LocalDate.of(2026, 1, 2);

    LoanRepositoryImpl repository();

    EntityManager entityManager();

    JdbcTemplate jdbc();

    private void flushAndClear() {
        entityManager().flush();
        entityManager().clear();
    }

    private Loan open(Long bookId, Long userId) {
        return Loan.open(bookId, userId, DAY);
    }

    @Test
    default void save_assignsIdAndKeepsFields() {
        Loan saved = repository().save(open(10L, 20L));
        flushAndClear();

        assertNotNull(saved.id());
        Loan reloaded = repository().findById(saved.id()).orElseThrow();
        assertEquals(10L, reloaded.bookId());
        assertEquals(20L, reloaded.userId());
        assertEquals(DAY, reloaded.loanDate());
        assertNull(reloaded.returnDate());
    }

    @Test
    default void save_existing_updatesTheReturnDateWithoutDuplicating() {
        Loan saved = repository().save(open(10L, 20L));
        flushAndClear();

        repository().save(saved.markReturned(DAY.plusDays(7)));
        flushAndClear();

        assertEquals(DAY.plusDays(7), repository().findById(saved.id()).orElseThrow().returnDate());
        assertEquals(1, repository().findAll().size());
    }

    @Test
    default void save_missingId_throwsNotFound() {
        Loan ghost = new Loan(99999L, 10L, 20L, DAY, null);

        assertThrows(LoanNotFoundException.class, () -> repository().save(ghost));
    }

    @Test
    default void save_staleLoan_translatesTheOptimisticLockToADomainException() {
        Loan saved = repository().save(open(10L, 20L));
        flushAndClear();
        Loan loaded = repository().findById(saved.id()).orElseThrow(); // entidad gestionada con su versión
        // Otra transacción modifica el préstamo y sube la versión
        jdbc().update("update prestamos set version = version + 1 where id = ?", saved.id());

        assertThrows(LoanConcurrentModificationException.class,
                () -> repository().save(loaded.markReturned(DAY.plusDays(1))));
    }

    @Test
    default void findById_withoutResultIsEmpty() {
        assertTrue(repository().findById(9999L).isEmpty());
    }

    @Test
    default void findByUserId_returnsOnlyThatUsersLoans() {
        repository().save(open(10L, 20L));
        repository().save(open(11L, 20L));
        repository().save(open(12L, 21L));
        flushAndClear();

        List<Loan> result = repository().findByUserId(20L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.userId().equals(20L)));
        assertTrue(repository().findByUserId(99L).isEmpty());
    }

    @Test
    default void existsByUserIdAndByBookId() {
        repository().save(open(10L, 20L));
        flushAndClear();

        assertTrue(repository().existsByUserId(20L));
        assertFalse(repository().existsByUserId(21L));
        assertTrue(repository().existsByBookId(10L));
        assertFalse(repository().existsByBookId(11L));
    }

    /** La tabla y las columnas históricas se mantienen. */
    @Test
    default void usesTheHistoricalTableAndColumns() {
        Loan loan = new Loan(null, 10L, 20L, DAY, DAY.plusDays(7));
        Long id = repository().save(loan).id();
        flushAndClear();

        Integer count = jdbc().queryForObject(
                "select count(*) from prestamos where id = ? and libro_id = 10 and usuario_id = 20 "
                        + "and fecha_prestamo = '2026-01-02' and fecha_devolucion = '2026-01-09' and version = 0",
                Integer.class, id);

        assertEquals(1, count);
    }
}

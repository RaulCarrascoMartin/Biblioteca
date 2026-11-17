package com.example.demo.loans.internal.application;

import com.example.demo.catalog.spi.BookInUseChecker;
import com.example.demo.loans.internal.domain.service.LoanQueryService;
import org.springframework.stereotype.Component;

/**
 * Adaptador hacia {@code catalog}: un libro con préstamos registrados no puede retirarse (se conserva
 * el historial). Usa {@link LoanQueryService}, que solo depende del repositorio y no cierra ciclos de
 * beans con quien consulta a los checkers al borrar.
 */
@Component
public class BookWithLoansChecker implements BookInUseChecker {

    private final LoanQueryService loanQueryService;

    public BookWithLoansChecker(LoanQueryService loanQueryService) {
        this.loanQueryService = loanQueryService;
    }

    @Override
    public boolean isInUse(Long bookId) {
        return loanQueryService.existsByBookId(bookId);
    }
}

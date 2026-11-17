package com.example.demo.loans.internal.application;

import com.example.demo.catalog.BookInUseChecker;
import com.example.demo.loans.internal.domain.repository.LoanRepository;
import org.springframework.stereotype.Component;

/**
 * Adaptador hacia {@code catalog}: un libro con préstamos registrados no puede retirarse (se conserva
 * el historial). Consulta el repositorio propio y no el servicio, porque el servicio depende de
 * {@code CatalogModule} y cerraría un ciclo de beans con quien consulta a los checkers al borrar.
 */
@Component
public class BookWithLoansChecker implements BookInUseChecker {

    private final LoanRepository loanRepository;

    public BookWithLoansChecker(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public boolean isInUse(Long bookId) {
        return loanRepository.existsByBookId(bookId);
    }
}

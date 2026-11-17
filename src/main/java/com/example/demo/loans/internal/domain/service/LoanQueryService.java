package com.example.demo.loans.internal.domain.service;

/**
 * Consultas que otros módulos necesitan a través de los checkers. Depende solo de {@code LoanRepository}
 * (no de catalog ni users), así no cierra un ciclo de beans con quien consulta a los checkers al borrar.
 */
public interface LoanQueryService {

    boolean existsByBookId(Long bookId);

    boolean existsByUserId(Long userId);
}

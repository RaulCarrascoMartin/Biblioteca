package com.example.demo.loans.internal.domain.service;

import com.example.demo.loans.dto.LoanResponseDto;
import com.example.demo.loans.internal.domain.dto.CreateLoanDto;

import java.util.List;

public interface LoanService {

    List<LoanResponseDto> getAll();

    /** @throws com.example.demo.loans.internal.domain.exception.LoanNotFoundException si no existe */
    LoanResponseDto getById(Long id);

    /** Préstamos de un usuario; vacío si no tiene. */
    List<LoanResponseDto> getByUserId(Long userId);

    /**
     * Presta un libro: lo reserva en el catálogo y registra el préstamo.
     *
     * @throws com.example.demo.loans.internal.domain.exception.LoanUserNotFoundException si el usuario no existe
     * @throws com.example.demo.catalog.exception.BookNotFoundException si el libro no existe
     * @throws com.example.demo.catalog.exception.BookNotAvailableException si el libro ya está prestado
     */
    LoanResponseDto create(CreateLoanDto dto);

    /**
     * Registra la devolución y vuelve a dejar el libro disponible.
     *
     * @throws com.example.demo.loans.internal.domain.exception.LoanNotFoundException si no existe
     * @throws com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException si ya estaba devuelto
     */
    LoanResponseDto returnLoan(Long id);

    boolean existsByBookId(Long bookId);

    boolean existsByUserId(Long userId);
}

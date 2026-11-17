package com.example.demo.loans.internal.domain.service;

import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.dto.LoanDto;

import java.util.List;

public interface LoanService {

    List<LoanDto> getAll();

    /** @throws com.example.demo.loans.internal.domain.exception.LoanNotFoundException si no existe */
    LoanDto getById(Long id);

    /** Préstamos de un usuario; vacío si no tiene. */
    List<LoanDto> getByUserId(Long userId);

    /**
     * Presta un libro: lo reserva en el catálogo y registra el préstamo.
     *
     * @throws com.example.demo.loans.internal.domain.exception.LoanUserNotFoundException si el usuario no existe
     * @throws com.example.demo.loans.internal.domain.exception.LoanBookNotFoundException si el libro no existe
     * @throws com.example.demo.loans.internal.domain.exception.LoanBookNotAvailableException si el libro ya está prestado
     */
    LoanDto create(CreateLoanDto dto);

    /**
     * Registra la devolución y vuelve a dejar el libro disponible.
     *
     * @throws com.example.demo.loans.internal.domain.exception.LoanNotFoundException si no existe
     * @throws com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException si ya estaba devuelto
     * @throws com.example.demo.loans.internal.domain.exception.LoanConcurrentModificationException si otra operación lo modificó a la vez
     */
    LoanDto returnLoan(Long id);
}

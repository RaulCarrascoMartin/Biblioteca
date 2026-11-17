package com.example.demo.loans.internal.domain.dto;

import com.example.demo.catalog.dto.BookSummary;

import java.time.LocalDate;

/** DTO interno del dominio: el préstamo con los datos del libro y del usuario ya resueltos. */
public record LoanDto(
        Long id,
        LocalDate loanDate,
        LocalDate returnDate,
        BookSummary book,
        Long userId,
        String userName,
        String userEmail
) {
}

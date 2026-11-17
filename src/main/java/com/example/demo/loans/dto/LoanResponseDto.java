package com.example.demo.loans.dto;

import com.example.demo.catalog.dto.BookResponseDto;

import java.time.LocalDate;

/** DTO público del préstamo, con los datos del libro y del usuario ya resueltos. */
public record LoanResponseDto(
        Long id,
        LocalDate loanDate,
        LocalDate returnDate,
        BookResponseDto book,
        Long userId,
        String userName,
        String userEmail
) {
}

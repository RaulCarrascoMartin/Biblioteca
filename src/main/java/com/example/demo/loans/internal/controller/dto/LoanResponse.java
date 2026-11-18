package com.example.demo.loans.internal.controller.dto;


import java.time.LocalDate;

/** DTO web de salida: el contrato HTTP del préstamo . */
public record LoanResponse(
        Long id,
        LocalDate loanDate,
        LocalDate returnDate,
        LoanBookResponse book,
        Long userId,
        String userName,
        String userEmail
) {
}

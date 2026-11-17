package com.example.demo.loans.internal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/** DTO web de salida: el contrato HTTP del préstamo (los nombres JSON no cambian). */
public record LoanResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("fechaPrestamo") LocalDate loanDate,
        @JsonProperty("fechaDevolucion") LocalDate returnDate,
        @JsonProperty("libro") LoanBookResponse book,
        @JsonProperty("usuarioId") Long userId,
        @JsonProperty("usuarioNombre") String userName,
        @JsonProperty("usuarioEmail") String userEmail
) {
}

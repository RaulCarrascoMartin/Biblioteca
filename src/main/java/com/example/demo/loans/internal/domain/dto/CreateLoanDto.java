package com.example.demo.loans.internal.domain.dto;

/** Datos para crear un préstamo: el libro y el usuario a cuyo nombre se presta. */
public record CreateLoanDto(Long bookId, Long userId) {
}

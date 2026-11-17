package com.example.demo.loans.internal.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Modelo de dominio anémico. Referencia al libro y al usuario solo por id. */
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    private Long id;
    private Long bookId;
    private Long userId;
    private LocalDate loanDate;
    private LocalDate returnDate;
}

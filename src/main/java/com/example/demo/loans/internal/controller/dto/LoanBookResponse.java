package com.example.demo.loans.internal.controller.dto;

import com.example.demo.catalog.dto.Genre;

/** Libro anidado en la respuesta del préstamo. */
public record LoanBookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        boolean available,
        Genre genre
) {
}

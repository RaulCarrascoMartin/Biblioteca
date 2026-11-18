package com.example.demo.loans.internal.controller.dto;

import com.example.demo.catalog.dto.Genre;

/** Libro anidado en la respuesta del préstamo (los nombres JSON históricos no cambian). */
public record LoanBookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        boolean available,
        Genre genre
) {
}

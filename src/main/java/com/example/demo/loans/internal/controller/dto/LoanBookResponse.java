package com.example.demo.loans.internal.controller.dto;

import com.example.demo.catalog.dto.Genre;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Libro anidado en la respuesta del préstamo (los nombres JSON históricos no cambian). */
public record LoanBookResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("titulo") String title,
        @JsonProperty("autor") String author,
        @JsonProperty("isbn") String isbn,
        @JsonProperty("disponible") boolean available,
        @JsonProperty("genero") Genre genre
) {
}

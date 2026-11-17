package com.example.demo.catalog.internal.controller.dto;

import com.example.demo.catalog.dto.Genre;
import com.fasterxml.jackson.annotation.JsonProperty;

/** DTO web de salida: el contrato HTTP del libro (los nombres JSON no cambian). */
public record BookResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("titulo") String title,
        @JsonProperty("autor") String author,
        @JsonProperty("isbn") String isbn,
        @JsonProperty("disponible") boolean available,
        @JsonProperty("genero") Genre genre
) {
}

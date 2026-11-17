package com.example.demo.catalog.internal.domain.dto;

import com.example.demo.catalog.internal.domain.model.Genre;

/** Contrato del servicio: lo que devuelve sobre un libro. El modelo no sale del servicio. */
public record BookDto(Long id, String title, String author, String isbn, boolean available, Genre genre) {
}

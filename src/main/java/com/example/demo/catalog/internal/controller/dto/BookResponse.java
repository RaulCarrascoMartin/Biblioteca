package com.example.demo.catalog.internal.controller.dto;

import com.example.demo.catalog.dto.Genre;

/** DTO web de salida: el contrato HTTP del libro . */
public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        boolean available,
        Genre genre
) {
}

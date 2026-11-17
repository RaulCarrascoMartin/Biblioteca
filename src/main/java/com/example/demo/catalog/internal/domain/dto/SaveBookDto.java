package com.example.demo.catalog.internal.domain.dto;

import com.example.demo.catalog.dto.Genre;

/** Contrato del servicio para crear o actualizar un libro. Sin validación Jakarta: es del DTO web. */
public record SaveBookDto(String title, String author, String isbn, Genre genre) {
}

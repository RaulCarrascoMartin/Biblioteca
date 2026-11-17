package com.example.demo.catalog.dto;

/** DTO público del libro: lo que otros módulos reciben a través de {@code CatalogModule}. */
public record BookSummary(Long id, String title, String author, String isbn, boolean available, Genre genre) {
}

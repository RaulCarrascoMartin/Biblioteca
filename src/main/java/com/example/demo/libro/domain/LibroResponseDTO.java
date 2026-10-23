package com.example.demo.libro.domain;

public record LibroResponseDTO(Long id, String titulo, String autor, String isbn, boolean disponible, Genero genero) {

    public LibroResponseDTO(Libro libro) {
        this(libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getIsbn(), libro.isDisponible(), libro.getGenero());
    }
}

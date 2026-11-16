package com.example.demo.libro.dto;

public record LibroResponseDTO(Long id, String titulo, String autor, String isbn, boolean disponible, Genero genero) {
}

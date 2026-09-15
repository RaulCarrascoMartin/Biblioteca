package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Genero;
import com.example.demo.domain.entity.Libro;
import lombok.Data;

@Data
public class LibroResponseDTO {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private boolean disponible;
    private Genero genero;

    public LibroResponseDTO(Libro libro) {
        this.id = libro.getId();
        this.titulo = libro.getTitulo();
        this.autor = libro.getAutor();
        this.isbn = libro.getIsbn();
        this.disponible = libro.isDisponible();
        this.genero = libro.getGenero();
    }
}

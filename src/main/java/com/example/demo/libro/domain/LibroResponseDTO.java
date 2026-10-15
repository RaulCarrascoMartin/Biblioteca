package com.example.demo.libro.domain;

import com.example.demo.libro.domain.Genero;
import com.example.demo.libro.domain.Libro;
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

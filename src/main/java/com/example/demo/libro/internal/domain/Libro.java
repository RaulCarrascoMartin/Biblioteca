package com.example.demo.libro.internal.domain;

import com.example.demo.libro.dto.Genero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Modelo de dominio anémico: solo datos, sin persistencia ni HTTP. */
@Getter
@Setter
@NoArgsConstructor
public class Libro {

    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private boolean disponible = true;
    private Genero genero;
}

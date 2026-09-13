package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "libros")
@Data

public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String autor;

    @Column(unique = true)
    private String isbn;

    private boolean disponible = true;

    @Enumerated(EnumType.STRING)
    private Genero genero;


}

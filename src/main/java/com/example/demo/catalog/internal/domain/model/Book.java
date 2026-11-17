package com.example.demo.catalog.internal.domain.model;

import com.example.demo.catalog.dto.Genre;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Modelo de dominio anémico: solo datos, sin persistencia ni HTTP. */
@Getter
@Setter
@NoArgsConstructor
public class Book {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private boolean available = true;
    private Genre genre;
}

package com.example.demo.catalog.internal.domain.model;

import com.example.demo.catalog.internal.domain.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotAvailableException;
import com.example.demo.catalog.internal.domain.exception.InvalidBookException;

/** Libro del catálogo. Inmutable: sus operaciones devuelven un libro nuevo. */
public record Book(Long id, String title, String author, Isbn isbn, boolean available, Genre genre) {

    public Book {
        if (title == null || title.isBlank()) {
            throw new InvalidBookException("El título es obligatorio");
        }
        if (author == null || author.isBlank()) {
            throw new InvalidBookException("El autor es obligatorio");
        }
        if (isbn == null) {
            throw new InvalidBookException("El ISBN es obligatorio");
        }
        if (genre == null) {
            throw new InvalidBookException("El género es obligatorio");
        }
    }

    /** Libro sin persistir (sin id) y disponible. */
    public static Book newBook(String title, String author, Isbn isbn, Genre genre) {
        return new Book(null, title, author, isbn, true, genre);
    }

    /** Edita los datos del libro; un género nulo conserva el actual. */
    public Book withDetails(String title, String author, Isbn isbn, Genre genre) {
        return new Book(id, title, author, isbn, available, genre == null ? this.genre : genre);
    }

    /** Lo marca como prestado. */
    public Book reserve() {
        if (!available) {
            throw new BookNotAvailableException();
        }
        return new Book(id, title, author, isbn, false, genre);
    }

    /** Lo vuelve a dejar disponible. */
    public Book release() {
        if (available) {
            throw new BookAlreadyAvailableException();
        }
        return new Book(id, title, author, isbn, true, genre);
    }
}

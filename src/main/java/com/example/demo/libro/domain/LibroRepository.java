package com.example.demo.libro.domain;

import com.example.demo.libro.domain.Libro;

import java.util.List;
import java.util.Optional;

public interface LibroRepository {
    List<Libro> findAll();

    Optional<Libro> findById(Long id);

    Optional<Libro> findByIsbn(String isbn);

    /** Lectura con bloqueo de escritura; debe llamarse dentro de una transacción. */
    Optional<Libro> findByIdWithLock(Long id);

    Libro save(Libro libro);

    void deleteById(Long id);
}

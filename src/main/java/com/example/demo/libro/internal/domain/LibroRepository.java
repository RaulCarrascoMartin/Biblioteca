package com.example.demo.libro.internal.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio; lo implementa {@code persistence} (inversión de dependencias). */
public interface LibroRepository {
    List<Libro> findAll();

    Optional<Libro> findById(Long id);

    List<Libro> findAllById(Collection<Long> ids);

    Optional<Libro> findByIsbn(String isbn);

    /** Lectura con bloqueo de escritura; debe llamarse dentro de una transacción. */
    Optional<Libro> findByIdWithLock(Long id);

    Libro save(Libro libro);

    void deleteById(Long id);
}

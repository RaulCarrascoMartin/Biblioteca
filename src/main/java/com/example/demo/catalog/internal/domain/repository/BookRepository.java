package com.example.demo.catalog.internal.domain.repository;

import com.example.demo.catalog.internal.domain.model.Book;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio; lo implementa {@code persistence} (inversión de dependencias). */
public interface BookRepository {

    List<Book> findAll();

    Optional<Book> findById(Long id);

    List<Book> findAllById(Collection<Long> ids);

    Optional<Book> findByIsbn(String isbn);

    /** Lectura con bloqueo de escritura; debe llamarse dentro de una transacción. */
    Optional<Book> findByIdWithLock(Long id);

    Book save(Book book);

    void deleteById(Long id);
}

package com.example.demo.catalog.internal.domain.service;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookService {

    List<BookDto> getAll();

    /** Vacío si no existe (lo usa la API pública del módulo). */
    Optional<BookDto> findById(Long id);

    /** @throws com.example.demo.catalog.internal.domain.exception.BookNotFoundException si no existe */
    BookDto getById(Long id);

    /** Varios libros de una vez (evita N consultas), indexados por id. Los ids inexistentes se omiten. */
    Map<Long, BookDto> getByIds(Collection<Long> ids);

    /** @throws com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException si el ISBN ya existe */
    BookDto create(SaveBookDto dto);

    /**
     * El género nulo no sobrescribe el actual.
     *
     * @throws com.example.demo.catalog.internal.domain.exception.BookNotFoundException si no existe
     * @throws com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException si el ISBN es de otro libro
     */
    BookDto update(Long id, SaveBookDto dto);

    /**
     * Marca el libro como no disponible (con bloqueo) al prestarlo.
     *
     * @throws com.example.demo.catalog.internal.domain.exception.BookNotFoundException si no existe
     * @throws com.example.demo.catalog.internal.domain.exception.BookNotAvailableException si ya está prestado
     */
    BookDto reserve(Long id);

    /**
     * Vuelve a dejar el libro disponible (con bloqueo) al devolverlo.
     *
     * @throws com.example.demo.catalog.internal.domain.exception.BookNotFoundException si no existe
     * @throws com.example.demo.catalog.internal.domain.exception.BookAlreadyAvailableException si ya estaba disponible
     */
    BookDto release(Long id);

    /**
     * @throws com.example.demo.catalog.internal.domain.exception.BookNotFoundException si no existe
     * @throws com.example.demo.catalog.internal.domain.exception.BookWithLoansException si algún
     *         {@code BookInUseChecker} lo veta
     */
    void delete(Long id);
}

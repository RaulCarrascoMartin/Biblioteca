package com.example.demo.catalog.internal.domain.service;

import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.dto.SeedBooksDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookService {

    List<BookResponseDto> getAll();

    /** Vacío si no existe (lo usa la API pública del módulo). */
    Optional<BookResponseDto> findById(Long id);

    /** @throws com.example.demo.catalog.exception.BookNotFoundException si no existe */
    BookResponseDto getById(Long id);

    /** Varios libros de una vez (evita N consultas), indexados por id. Los ids inexistentes se omiten. */
    Map<Long, BookResponseDto> getByIds(Collection<Long> ids);

    /** @throws com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException si el ISBN ya existe */
    BookResponseDto create(SaveBookDto dto);

    /**
     * El género nulo no sobrescribe el actual.
     *
     * @throws com.example.demo.catalog.exception.BookNotFoundException si no existe
     * @throws com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException si el ISBN es de otro libro
     */
    BookResponseDto update(Long id, SaveBookDto dto);

    /** Marca el libro como no disponible (con bloqueo) al prestarlo. */
    BookResponseDto reserve(Long id);

    /** Vuelve a dejar el libro disponible (con bloqueo) al devolverlo. */
    BookResponseDto release(Long id);

    /**
     * @throws com.example.demo.catalog.exception.BookNotFoundException si no existe
     * @throws com.example.demo.catalog.internal.domain.exception.BookWithLoansException si algún
     *         {@code BookInUseChecker} lo veta
     */
    void delete(Long id);

    /** Añade los libros que faltan y asigna el género a los existentes que no lo tengan igual. */
    SeedBooksDto seed(List<SaveBookDto> catalog);
}

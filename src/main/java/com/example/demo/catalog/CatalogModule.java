package com.example.demo.catalog;

import com.example.demo.catalog.dto.BookResponseDto;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * API pública del módulo {@code catalog}. Es lo único que otros módulos pueden usar: recibe y
 * devuelve DTO, nunca el modelo interno. Las excepciones de {@code reserve} y {@code release} son
 * públicas (paquete {@code catalog.exception}).
 */
public interface CatalogModule {

    Optional<BookResponseDto> getById(Long id);

    /** Varios libros de una vez, indexados por id. Los ids inexistentes se omiten. */
    Map<Long, BookResponseDto> getByIds(Collection<Long> ids);

    /**
     * Marca el libro como no disponible (con bloqueo).
     *
     * @throws com.example.demo.catalog.exception.BookNotFoundException si no existe
     * @throws com.example.demo.catalog.exception.BookNotAvailableException si ya está prestado
     */
    BookResponseDto reserve(Long id);

    /**
     * Vuelve a dejar el libro disponible (con bloqueo).
     *
     * @throws com.example.demo.catalog.exception.BookNotFoundException si no existe
     * @throws com.example.demo.catalog.exception.BookAlreadyAvailableException si ya estaba disponible
     */
    BookResponseDto release(Long id);
}

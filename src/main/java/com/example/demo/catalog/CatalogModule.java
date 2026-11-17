package com.example.demo.catalog;

import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.ReservationResult;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * API pública del módulo {@code catalog}. Es lo único que otros módulos pueden usar: recibe y
 * devuelve DTO públicos, nunca el modelo interno ni sus excepciones.
 */
public interface CatalogModule {

    Optional<BookSummary> getById(Long id);

    /** Varios libros de una vez, indexados por id. Los ids inexistentes se omiten. */
    Map<Long, BookSummary> getByIds(Collection<Long> ids);

    /**
     * Marca el libro como no disponible (con bloqueo).
     *
     * @return {@code OK} con el libro, {@code NOT_FOUND} si no existe o {@code NOT_AVAILABLE} si ya está prestado
     */
    ReservationResult reserve(Long id);

    /**
     * Vuelve a dejar el libro disponible (con bloqueo).
     *
     * @return {@code OK} con el libro, {@code NOT_FOUND} si no existe o {@code ALREADY_AVAILABLE} si ya lo estaba
     */
    ReservationResult release(Long id);
}

package com.example.demo.libro;

import com.example.demo.libro.dto.LibroResponseDTO;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * API pública del módulo {@code libro}. Es lo único que otros módulos pueden usar: recibe y
 * devuelve DTO, nunca el modelo interno.
 */
public interface LibroModule {

    Optional<LibroResponseDTO> obtenerPorId(Long id);

    /** Varios libros de una vez, indexados por id. Los ids inexistentes se omiten. */
    Map<Long, LibroResponseDTO> obtenerPorIds(Collection<Long> ids);

    /** Marca el libro como no disponible (con bloqueo). Falla si no existe o ya está prestado. */
    LibroResponseDTO reservar(Long id);

    /** Vuelve a dejar el libro disponible (con bloqueo). Falla si no existe. */
    LibroResponseDTO liberar(Long id);
}

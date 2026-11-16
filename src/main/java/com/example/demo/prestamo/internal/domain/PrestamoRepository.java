package com.example.demo.prestamo.internal.domain;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio; lo implementa {@code persistence} (inversión de dependencias). */
public interface PrestamoRepository {
    List<Prestamo> findAll();

    Optional<Prestamo> findById(Long id);

    List<Prestamo> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByLibroId(Long libroId);

    Prestamo save(Prestamo prestamo);
}

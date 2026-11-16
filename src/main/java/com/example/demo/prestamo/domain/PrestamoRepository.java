package com.example.demo.prestamo.domain;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository {
    List<Prestamo> findAll();

    Optional<Prestamo> findById(Long id);

    List<Prestamo> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByLibroId(Long libroId);

    Prestamo save(Prestamo prestamo);
}

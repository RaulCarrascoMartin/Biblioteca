package com.example.demo.prestamo.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPrestamoRepository extends JpaRepository<PrestamoEntity, Long> {
    List<PrestamoEntity> findByFechaDevolucionIsNull();

    List<PrestamoEntity> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByLibroId(Long libroId);
}

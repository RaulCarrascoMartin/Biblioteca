package com.example.demo.persistance.repository;

import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findByFechaDevolucionIsNull();

    List<Prestamo> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByLibroId(Long libroId);
}

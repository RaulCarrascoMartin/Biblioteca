package com.example.demo.prestamo.persistence;

import com.example.demo.libro.domain.Libro;
import com.example.demo.prestamo.domain.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaPrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findByFechaDevolucionIsNull();

    List<Prestamo> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByLibroId(Long libroId);
}

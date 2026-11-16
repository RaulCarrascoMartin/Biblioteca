package com.example.demo.prestamo.internal.domain;

import com.example.demo.libro.LibroEnUsoChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Un libro con préstamos registrados no puede retirarse (se conserva el historial). */
@Component
@RequiredArgsConstructor
public class LibroConPrestamosChecker implements LibroEnUsoChecker {

    private final PrestamoRepository prestamoRepository;

    @Override
    public boolean estaEnUso(Long libroId) {
        return prestamoRepository.existsByLibroId(libroId);
    }
}

package com.example.demo.prestamo.domain;

import com.example.demo.libro.domain.LibroResponseDTO;

import java.time.LocalDate;

public record PrestamoResponseDTO(
        Long id,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucion,
        LibroResponseDTO libro,
        Long usuarioId,
        String usuarioNombre,
        String usuarioEmail
) {

    public PrestamoResponseDTO(Prestamo prestamo) {
        this(prestamo.getId(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucion(),
                new LibroResponseDTO(prestamo.getLibro()),
                prestamo.getUsuario().getId(),
                prestamo.getUsuario().getNombre(),
                prestamo.getUsuario().getEmail());
    }
}

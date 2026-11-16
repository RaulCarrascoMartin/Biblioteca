package com.example.demo.prestamo.dto;

import com.example.demo.libro.dto.LibroResponseDTO;
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
}

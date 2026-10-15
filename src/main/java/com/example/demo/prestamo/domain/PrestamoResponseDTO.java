package com.example.demo.prestamo.domain;

import com.example.demo.libro.domain.LibroResponseDTO;

import com.example.demo.prestamo.domain.Prestamo;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PrestamoResponseDTO {
    private Long id;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private LibroResponseDTO libro;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;

    public PrestamoResponseDTO(Prestamo prestamo) {
        this.id = prestamo.getId();
        this.fechaPrestamo = prestamo.getFechaPrestamo();
        this.fechaDevolucion = prestamo.getFechaDevolucion();
        this.libro = new LibroResponseDTO(prestamo.getLibro());
        this.usuarioId = prestamo.getUsuario().getId();
        this.usuarioNombre = prestamo.getUsuario().getNombre();
        this.usuarioEmail = prestamo.getUsuario().getEmail();
    }
}
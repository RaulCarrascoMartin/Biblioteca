package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.entity.Prestamo;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PrestamoResponseDTO {
    private Long id;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private Libro libro;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;

    public PrestamoResponseDTO(Prestamo prestamo) {
        this.id = prestamo.getId();
        this.fechaPrestamo = prestamo.getFechaPrestamo();
        this.fechaDevolucion = prestamo.getFechaDevolucion();
        this.libro = prestamo.getLibro();
        this.usuarioId = prestamo.getUsuario().getId();
        this.usuarioNombre = prestamo.getUsuario().getNombre();
        this.usuarioEmail = prestamo.getUsuario().getEmail();
    }
}
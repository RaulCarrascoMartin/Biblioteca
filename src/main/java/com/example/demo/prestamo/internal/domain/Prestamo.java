package com.example.demo.prestamo.internal.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Modelo de dominio anémico. Referencia a libro y usuario solo por ID. */
@Getter
@Setter
@NoArgsConstructor
public class Prestamo {

    private Long id;
    private Long libroId;
    private Long usuarioId;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
}

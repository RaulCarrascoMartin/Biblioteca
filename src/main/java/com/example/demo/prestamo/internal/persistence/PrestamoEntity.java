package com.example.demo.prestamo.internal.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "prestamos")
@Getter
@Setter
@NoArgsConstructor
public class PrestamoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Relación por ID: el modelo de {@code libro} es privado de ese módulo. */
    @Column(name = "libro_id", nullable = false)
    private Long libroId;

    /** Relación por ID: el modelo de {@code usuario} es privado de ese módulo. */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrestamoEntity prestamo = (PrestamoEntity) o;
        return Objects.equals(id, prestamo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PrestamoEntity{id=" + id + ", fechaPrestamo=" + fechaPrestamo + "}";
    }
}
package com.example.demo.loans.internal.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

/** Los nombres de tabla y columnas se fijan explícitamente para conservar el esquema existente. */
@Entity
@Table(name = "prestamos")
@Getter
@Setter
@NoArgsConstructor
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Relación por id: el modelo de {@code catalog} es privado de ese módulo. */
    @Column(name = "libro_id", nullable = false)
    private Long bookId;

    /** Relación por id: el modelo de {@code users} es privado de ese módulo. */
    @Column(name = "usuario_id", nullable = false)
    private Long userId;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate loanDate;

    @Column(name = "fecha_devolucion")
    private LocalDate returnDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanEntity loan = (LoanEntity) o;
        return Objects.equals(id, loan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LoanEntity{id=" + id + ", loanDate=" + loanDate + "}";
    }
}

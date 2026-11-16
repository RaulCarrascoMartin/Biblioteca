package com.example.demo.libro.internal.persistence;

import com.example.demo.libro.dto.Genero;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "libros")
@Getter
@Setter
@NoArgsConstructor
public class LibroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String autor;

    @Column(unique = true)
    private String isbn;

    private boolean disponible = true;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibroEntity libro = (LibroEntity) o;
        return Objects.equals(id, libro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LibroEntity{id=" + id + ", titulo='" + titulo + "', disponible=" + disponible + "}";
    }
}
package com.example.demo.catalog.internal.persistence;

import com.example.demo.catalog.dto.Genre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/** Los nombres de tabla y columnas se fijan explícitamente para conservar el esquema existente. */
@Entity
@Table(name = "libros")
@Getter
@Setter
@NoArgsConstructor
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "titulo")
    private String title;

    @Column(name = "autor")
    private String author;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @Column(name = "disponible")
    private boolean available = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero")
    private Genre genre;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookEntity book = (BookEntity) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BookEntity{id=" + id + ", title='" + title + "', available=" + available + "}";
    }
}

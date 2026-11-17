package com.example.demo.catalog.internal.persistence.dao.entity;

import com.example.demo.catalog.internal.domain.model.Genre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/** Los nombres de tabla y columnas se fijan explícitamente para conservar el esquema existente. */
@Entity
@Table(name = "libros")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "titulo")
    private String title;

    @Setter
    @Column(name = "autor")
    private String author;

    @Setter
    @Column(name = "isbn", unique = true)
    private String isbn;

    @Setter
    @Column(name = "disponible")
    private boolean available = true;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "genero")
    private Genre genre;

    /** Alta: el id lo asigna la base de datos. */
    public BookEntity(String title, String author, String isbn, boolean available, Genre genre) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = available;
        this.genre = genre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookEntity book = (BookEntity) o;
        return id != null && Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "BookEntity{id=" + id + ", title='" + title + "', available=" + available + "}";
    }
}

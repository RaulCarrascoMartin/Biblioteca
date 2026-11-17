package com.example.demo.catalog.internal.persistence.repository.mapper;

import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.model.Isbn;
import com.example.demo.catalog.internal.persistence.dao.entity.BookEntity;
import org.springframework.stereotype.Component;

/** Convierte entre la entidad JPA y el modelo de dominio. Sin reglas de negocio. */
@Component
public class BookPersistenceMapper {

    /** Reconstruye el modelo con su constructor: el dominio vuelve a comprobar sus invariantes. */
    public Book toModel(BookEntity entity) {
        return new Book(entity.getId(), entity.getTitle(), entity.getAuthor(), new Isbn(entity.getIsbn()),
                entity.isAvailable(), entity.getGenre());
    }

    /** Para modelos sin id; rechaza los que ya lo traen. */
    public BookEntity toNewEntity(Book book) {
        if (book.id() != null) {
            throw new IllegalArgumentException("A new book cannot have an id");
        }
        return new BookEntity(book.title(), book.author(), book.isbn().value(), book.available(), book.genre());
    }

    /** Copia el estado del modelo sobre una entidad gestionada. */
    public void updateManagedEntity(Book book, BookEntity entity) {
        entity.setTitle(book.title());
        entity.setAuthor(book.author());
        entity.setIsbn(book.isbn().value());
        entity.setAvailable(book.available());
        entity.setGenre(book.genre());
    }
}

package com.example.demo.catalog.internal.domain.mapper;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.model.Isbn;
import org.mapstruct.Mapper;

/** Convierte entre el modelo {@link Book} y sus DTO. Solo transforma datos, sin reglas de negocio. */
@Mapper(componentModel = "spring")
public interface BookMapper {

    default BookDto toDto(Book book) {
        return new BookDto(book.id(), book.title(), book.author(), book.isbn().value(), book.available(),
                book.genre());
    }

    /** Construye un libro nuevo; sus invariantes las comprueba el constructor del modelo. */
    default Book toModel(SaveBookDto dto) {
        return Book.newBook(dto.title(), dto.author(), new Isbn(dto.isbn()), dto.genre());
    }
}

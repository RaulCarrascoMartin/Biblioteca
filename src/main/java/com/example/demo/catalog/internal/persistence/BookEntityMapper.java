package com.example.demo.catalog.internal.persistence;

import com.example.demo.catalog.internal.domain.model.Book;
import org.mapstruct.Mapper;

/** Convierte entre la entidad JPA y el modelo de dominio. */
@Mapper(componentModel = "spring")
public interface BookEntityMapper {

    Book toModel(BookEntity entity);

    BookEntity toEntity(Book book);
}

package com.example.demo.libro.internal.persistence;

import com.example.demo.libro.internal.domain.Libro;
import org.mapstruct.Mapper;

/** Convierte entre la entidad JPA y el modelo de dominio. */
@Mapper(componentModel = "spring")
public interface LibroEntityMapper {

    Libro toModel(LibroEntity entity);

    LibroEntity toEntity(Libro libro);
}

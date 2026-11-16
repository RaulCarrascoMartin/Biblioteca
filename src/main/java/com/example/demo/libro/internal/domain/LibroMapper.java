package com.example.demo.libro.internal.domain;

import com.example.demo.libro.dto.LibroDTO;
import com.example.demo.libro.dto.LibroResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Convierte entre el modelo {@link Libro} y sus DTO. Solo transforma datos, sin reglas de negocio. */
@Mapper(componentModel = "spring")
public interface LibroMapper {

    LibroResponseDTO toResponse(Libro libro);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disponible", ignore = true)
    Libro toModel(LibroDTO dto);

    /** Los campos nulos del DTO (p. ej. el género) no sobrescriben el valor actual. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disponible", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void actualizar(LibroDTO dto, @MappingTarget Libro libro);
}

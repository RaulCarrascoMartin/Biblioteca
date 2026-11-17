package com.example.demo.catalog.internal.domain.mapper;

import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.model.Book;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Convierte entre el modelo {@link Book} y sus DTO. Solo transforma datos, sin reglas de negocio. */
@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponseDto toResponse(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "available", ignore = true)
    Book toModel(SaveBookDto dto);

    /** Los campos nulos del DTO (p. ej. el género) no sobrescriben el valor actual. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "available", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(SaveBookDto dto, @MappingTarget Book book);
}

package com.example.demo.catalog.internal.controller.mapper;

import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Convierte entre los DTO web y los del servicio. El género público ({@code catalog.dto.Genre}) se
 * mapea por nombre al del dominio y viceversa. Solo transforma datos.
 */
@Mapper(componentModel = "spring")
public interface BookWebMapper {

    SaveBookDto toDto(BookRequest request);

    BookResponse toResponse(BookDto dto);

    List<BookResponse> toResponses(List<BookDto> dtos);
}

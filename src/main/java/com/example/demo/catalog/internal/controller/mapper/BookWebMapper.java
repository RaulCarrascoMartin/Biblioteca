package com.example.demo.catalog.internal.controller.mapper;

import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import org.mapstruct.Mapper;

import java.util.List;

/** Convierte entre los DTO web y los del servicio. Solo transforma datos. */
@Mapper(componentModel = "spring")
public interface BookWebMapper {

    SaveBookDto toDto(BookRequest request);

    BookResponse toResponse(BookResponseDto dto);

    List<BookResponse> toResponses(List<BookResponseDto> dtos);
}

package com.example.demo.prestamo.internal.domain;

import com.example.demo.libro.dto.LibroResponseDTO;
import com.example.demo.prestamo.dto.PrestamoResponseDTO;
import com.example.demo.usuario.dto.UsuarioResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Compone la respuesta a partir del préstamo y los DTO públicos de {@code libro} y {@code usuario}.
 * El préstamo solo guarda sus ids; nunca ve los modelos de otros módulos.
 */
@Mapper(componentModel = "spring")
public interface PrestamoMapper {

    @Mapping(target = "id", source = "prestamo.id")
    @Mapping(target = "libro", source = "libro")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNombre", source = "usuario.nombre")
    @Mapping(target = "usuarioEmail", source = "usuario.email")
    PrestamoResponseDTO toResponse(Prestamo prestamo, LibroResponseDTO libro, UsuarioResponseDTO usuario);
}

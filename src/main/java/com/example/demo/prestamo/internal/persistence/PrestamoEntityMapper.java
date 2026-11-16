package com.example.demo.prestamo.internal.persistence;

import com.example.demo.prestamo.internal.domain.Prestamo;
import org.mapstruct.Mapper;

/** Convierte entre la entidad JPA y el modelo de dominio. */
@Mapper(componentModel = "spring")
public interface PrestamoEntityMapper {

    Prestamo toModel(PrestamoEntity entity);

    PrestamoEntity toEntity(Prestamo prestamo);
}

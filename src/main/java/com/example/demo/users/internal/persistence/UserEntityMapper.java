package com.example.demo.users.internal.persistence;

import com.example.demo.users.internal.domain.model.User;
import org.mapstruct.Mapper;

/** Convierte entre la entidad JPA y el modelo de dominio. */
@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    User toModel(UserEntity entity);

    UserEntity toEntity(User user);
}

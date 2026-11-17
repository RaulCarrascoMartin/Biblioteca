package com.example.demo.users.internal.domain.mapper;

import com.example.demo.users.internal.domain.dto.NewUserDto;
import com.example.demo.users.internal.domain.dto.UserCredentialsDto;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.model.Email;
import com.example.demo.users.internal.domain.model.Role;
import com.example.demo.users.internal.domain.model.User;
import org.mapstruct.Mapper;

/**
 * Convierte entre el modelo {@link User} y los DTO del servicio. Solo transforma datos: las
 * invariantes se comprueban en los constructores del modelo.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /** Nunca expone la contraseña. */
    UserDto toDto(User user);

    UserCredentialsDto toCredentialsDto(User user);

    /** Reconstruye un usuario existente; las invariantes del modelo se comprueban aquí. */
    default User toModel(UserDto dto, String passwordHash, int tokenVersion) {
        return new User(dto.id(), dto.name(), new Email(dto.email()), passwordHash,
                Role.valueOf(dto.role()), tokenVersion);
    }

    /** Usuario nuevo con rol USER a partir del alta; {@code passwordHash} ya viene hasheado. */
    default User toModel(NewUserDto dto, String passwordHash) {
        return User.newUser(dto.name(), new Email(dto.email()), passwordHash);
    }

    default String map(Email email) {
        return email == null ? null : email.value();
    }
}

package com.example.demo.auth.internal.domain.mapper;

import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.users.dto.NewUser;
import com.example.demo.users.dto.UserSummary;
import org.mapstruct.Mapper;

/** Convierte entre los DTO del servicio de auth y los DTO públicos de users. Solo transforma datos. */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    NewUser toNewUser(RegisterUserDto dto);

    RegisteredUserDto toRegisteredUser(UserSummary user);
}

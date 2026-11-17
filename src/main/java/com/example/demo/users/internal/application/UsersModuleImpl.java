package com.example.demo.users.internal.application;

import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.domain.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/** Implementación de la API pública: solo delega en el servicio, sin políticas de usuario. */
@Component
public class UsersModuleImpl implements UsersModule {

    private final UserService userService;

    public UsersModuleImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<UserResponseDto> getById(Long id) {
        return userService.findById(id);
    }

    @Override
    public Optional<UserResponseDto> getByEmail(String email) {
        return userService.findByEmail(email);
    }

    @Override
    public Map<Long, UserResponseDto> getByIds(Collection<Long> ids) {
        return userService.getByIds(ids);
    }

    @Override
    public Optional<UserResponseDto> register(UserDto dto) {
        return userService.register(dto);
    }

    @Override
    public Optional<UserCredentialsDto> getCredentials(String email) {
        return userService.findCredentialsByEmail(email);
    }
}

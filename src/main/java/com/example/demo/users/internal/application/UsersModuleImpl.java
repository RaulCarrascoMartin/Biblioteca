package com.example.demo.users.internal.application;

import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.NewUser;
import com.example.demo.users.dto.UserCredentials;
import com.example.demo.users.dto.UserSummary;
import com.example.demo.users.internal.domain.dto.NewUserDto;
import com.example.demo.users.internal.domain.dto.UserCredentialsDto;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Implementación de la API pública: delega en el servicio y mapea {@code XDto} a DTO público. */
@Component
public class UsersModuleImpl implements UsersModule {

    private final UserService userService;

    public UsersModuleImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<UserSummary> getById(Long id) {
        return userService.findById(id).map(UsersModuleImpl::toSummary);
    }

    @Override
    public Optional<UserSummary> getByEmail(String email) {
        return userService.findByEmail(email).map(UsersModuleImpl::toSummary);
    }

    @Override
    public Map<Long, UserSummary> getByIds(Collection<Long> ids) {
        return userService.getByIds(ids).values().stream()
                .map(UsersModuleImpl::toSummary)
                .collect(Collectors.toMap(UserSummary::id, Function.identity()));
    }

    @Override
    public Optional<UserSummary> register(NewUser dto) {
        return userService.register(new NewUserDto(dto.name(), dto.email(), dto.password()))
                .map(UsersModuleImpl::toSummary);
    }

    @Override
    public Optional<UserCredentials> getCredentials(String email) {
        return userService.findCredentialsByEmail(email).map(UsersModuleImpl::toCredentials);
    }

    @Override
    public void ensureAdmin(String email, String rawPassword) {
        userService.ensureAdmin(email, rawPassword);
    }

    private static UserSummary toSummary(UserDto dto) {
        return new UserSummary(dto.id(), dto.name(), dto.email(), dto.role());
    }

    private static UserCredentials toCredentials(UserCredentialsDto dto) {
        return new UserCredentials(dto.email(), dto.passwordHash(), dto.role(), dto.tokenVersion());
    }
}

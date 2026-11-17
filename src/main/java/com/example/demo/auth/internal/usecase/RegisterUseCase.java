package com.example.demo.auth.internal.usecase;

import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.service.AuthService;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Registra un usuario con rol USER. Flujo público. */
@Component
public class RegisterUseCase {

    private final AuthService authService;

    public RegisterUseCase(AuthService authService) {
        this.authService = authService;
    }

    public Optional<RegisteredUserDto> execute(RegisterUserDto dto) {
        return authService.register(dto);
    }
}

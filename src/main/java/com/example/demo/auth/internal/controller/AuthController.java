package com.example.demo.auth.internal.controller;

import com.example.demo.auth.internal.controller.dto.LoginRequest;
import com.example.demo.auth.internal.controller.dto.LoginResponse;
import com.example.demo.auth.internal.controller.dto.LogoutRequest;
import com.example.demo.auth.internal.controller.dto.LogoutResponse;
import com.example.demo.auth.internal.controller.dto.RefreshRequest;
import com.example.demo.auth.internal.controller.dto.RegisterRequest;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.auth.internal.controller.mapper.AuthWebMapper;
import com.example.demo.auth.internal.usecase.AuthUseCase;
import com.example.demo.shared.Requester;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Traduce HTTP a llamadas al caso de uso (DTO web ↔ DTO del servicio con {@link AuthWebMapper}). */
@RestController
public class AuthController implements AuthHttpApi {

    private final AuthUseCase authUseCase;
    private final AuthWebMapper mapper;

    public AuthController(AuthUseCase authUseCase, AuthWebMapper mapper) {
        this.authUseCase = authUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<RegisterResponse> register(RegisterRequest request) {
        return authUseCase.register(mapper.toDto(request))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return mapper.toResponse(authUseCase.login(request.email(), request.password()));
    }

    @Override
    public LoginResponse refresh(RefreshRequest request) {
        return mapper.toResponse(authUseCase.refresh(request.refreshToken()));
    }

    @Override
    public LogoutResponse logout(Requester requester, LogoutRequest request) {
        authUseCase.logout(requester, request.refreshToken());
        return new LogoutResponse("Sesión cerrada");
    }
}

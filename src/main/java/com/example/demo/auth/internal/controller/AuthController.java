package com.example.demo.auth.internal.controller;

import com.example.demo.auth.internal.controller.dto.LoginRequest;
import com.example.demo.auth.internal.controller.dto.LoginResponse;
import com.example.demo.auth.internal.controller.dto.LogoutRequest;
import com.example.demo.auth.internal.controller.dto.LogoutResponse;
import com.example.demo.auth.internal.controller.dto.RefreshRequest;
import com.example.demo.auth.internal.controller.dto.RegisterRequest;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.auth.internal.controller.mapper.AuthWebMapper;
import com.example.demo.auth.internal.usecase.LoginUseCase;
import com.example.demo.auth.internal.usecase.LogoutUseCase;
import com.example.demo.auth.internal.usecase.RefreshTokenUseCase;
import com.example.demo.auth.internal.usecase.RegisterUseCase;
import com.example.demo.shared.Requester;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Traduce HTTP a llamadas al caso de uso (DTO web ↔ DTO del servicio con {@link AuthWebMapper}). */
@RestController
public class AuthController implements AuthHttpApi {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final AuthWebMapper mapper;

    public AuthController(RegisterUseCase registerUseCase,
                          LoginUseCase loginUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          AuthWebMapper mapper) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<RegisterResponse> register(RegisterRequest request) {
        return registerUseCase.execute(mapper.toDto(request))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return mapper.toResponse(loginUseCase.execute(request.email(), request.password()));
    }

    @Override
    public LoginResponse refresh(RefreshRequest request) {
        return mapper.toResponse(refreshTokenUseCase.execute(request.refreshToken()));
    }

    @Override
    public LogoutResponse logout(Requester requester, LogoutRequest request) {
        logoutUseCase.execute(requester, request.refreshToken());
        return new LogoutResponse("Sesión cerrada");
    }
}

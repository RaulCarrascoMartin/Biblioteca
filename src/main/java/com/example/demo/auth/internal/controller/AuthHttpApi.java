package com.example.demo.auth.internal.controller;

import com.example.demo.auth.internal.controller.dto.LoginRequest;
import com.example.demo.auth.internal.controller.dto.LoginResponse;
import com.example.demo.auth.internal.controller.dto.LogoutRequest;
import com.example.demo.auth.internal.controller.dto.LogoutResponse;
import com.example.demo.auth.internal.controller.dto.RefreshRequest;
import com.example.demo.auth.internal.controller.dto.RegisterRequest;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.shared.Requester;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrato HTTP de autenticación (única fuente de verdad de rutas, validación y códigos). El
 * {@link Requester} lo inyecta la capa de seguridad y no forma parte del contrato.
 */
@Tag(name = "Autenticación", description = "Registro, login, refresh y logout con JWT de acceso y refresh opaco")
@RequestMapping("/api/auth")
public interface AuthHttpApi {

    @Operation(summary = "Registrar un usuario (público, siempre con rol USER)",
            description = "Si el email ya estaba registrado responde 200 sin cuerpo, para no revelar cuentas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario registrado (sin cuerpo si el email ya existía)"),
            @ApiResponse(responseCode = "400", description = "Datos con formato inválido"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos")
    })
    @PostMapping("/register")
    ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request);

    @Operation(summary = "Iniciar sesión (público)",
            description = "Devuelve el JWT de acceso (cabecera Authorization: Bearer) y el refresh token opaco.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Par de tokens"),
            @ApiResponse(responseCode = "400", description = "Datos con formato inválido"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos")
    })
    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Renovar el par de tokens (público)",
            description = "Rota el refresh token. Reutilizar uno ya usado cierra todas las sesiones del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Par de tokens nuevo"),
            @ApiResponse(responseCode = "400", description = "Datos con formato inválido"),
            @ApiResponse(responseCode = "401", description = "Refresh token desconocido, caducado o reutilizado")
    })
    @PostMapping("/refresh")
    LoginResponse refresh(@Valid @RequestBody RefreshRequest request);

    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión cerrada"),
            @ApiResponse(responseCode = "400", description = "Datos con formato inválido"),
            @ApiResponse(responseCode = "401", description = "Sin sesión")
    })
    @PostMapping("/logout")
    LogoutResponse logout(@Parameter(hidden = true) Requester requester,
                          @Valid @RequestBody LogoutRequest request);
}

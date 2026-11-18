package com.example.demo.users.internal.controller;

import com.example.demo.shared.Requester;
import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato HTTP de usuarios (única fuente de verdad de rutas, validación y códigos). El
 * {@link Requester} lo inyecta la capa de seguridad y no forma parte del contrato.
 */
@Tag(name = "Usuarios", description = "Gestión de cuentas de usuario")
@RequestMapping("/api/users")
public interface UserHttpApi {

    @Operation(summary = "Listar usuarios (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de usuarios"),
            @ApiResponse(responseCode = "401", description = "Sin sesión"),
            @ApiResponse(responseCode = "403", description = "Sin permiso")
    })
    @GetMapping
    List<UserResponse> getAll(@Parameter(hidden = true) Requester requester);

    @Operation(summary = "Usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actual"),
            @ApiResponse(responseCode = "401", description = "Sin sesión"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")
    })
    @GetMapping("/me")
    UserResponse me(@Parameter(hidden = true) Requester requester);

    @Operation(summary = "Obtener un usuario (ADMIN o dueño)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario"),
            @ApiResponse(responseCode = "401", description = "Sin sesión"),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")
    })
    @GetMapping("/{id}")
    UserResponse getById(@Parameter(hidden = true) Requester requester, @PathVariable("id") Long id);

    @Operation(summary = "Actualizar un usuario (ADMIN o dueño)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Formato inválido"),
            @ApiResponse(responseCode = "401", description = "Sin sesión"),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    @PutMapping("/{id}")
    UserResponse update(@Parameter(hidden = true) Requester requester, @PathVariable("id") Long id,
                        @Valid @RequestBody UserUpdateRequest request);

    @Operation(summary = "Revocar las sesiones de un usuario (ADMIN o dueño)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesiones revocadas"),
            @ApiResponse(responseCode = "401", description = "Sin sesión"),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")
    })
    @PostMapping("/{id}/revoke-sessions")
    ResponseEntity<Void> revokeSessions(@Parameter(hidden = true) Requester requester, @PathVariable("id") Long id);

    @Operation(summary = "Eliminar un usuario (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "401", description = "Sin sesión"),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe"),
            @ApiResponse(responseCode = "409", description = "Tiene préstamos o es el propio usuario")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@Parameter(hidden = true) Requester requester, @PathVariable("id") Long id);
}

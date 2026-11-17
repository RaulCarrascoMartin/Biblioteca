package com.example.demo.users.internal.controller;

import com.example.demo.shared.Requester;
import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.dto.UserUpdateRequest;
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
 * Contrato HTTP de usuarios (única fuente de verdad de rutas y validación). Códigos:
 * 200/204 éxito; 400 formato o email repetido; 401 sin sesión; 403 sin permiso; 404 no existe;
 * 409 usuario con préstamos o autoeliminación.
 */
@RequestMapping("/api/usuarios")
public interface UserHttpApi {

    /** Solo ADMIN. */
    @GetMapping
    List<UserResponse> getAll(Requester requester);

    /** Cualquier autenticado. */
    @GetMapping("/me")
    UserResponse me(Requester requester);

    /** ADMIN o dueño. */
    @GetMapping("/{id}")
    UserResponse getById(Requester requester, @PathVariable("id") Long id);

    /** ADMIN o dueño. */
    @PutMapping("/{id}")
    UserResponse update(Requester requester, @PathVariable("id") Long id, @Valid @RequestBody UserUpdateRequest request);

    /** ADMIN o dueño. */
    @PostMapping("/{id}/revocar-sesiones")
    ResponseEntity<Void> revokeSessions(Requester requester, @PathVariable("id") Long id);

    /** Solo ADMIN. */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(Requester requester, @PathVariable("id") Long id);
}

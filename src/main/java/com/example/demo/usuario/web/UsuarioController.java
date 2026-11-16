package com.example.demo.usuario.web;

import lombok.RequiredArgsConstructor;

import com.example.demo.usuario.domain.UsuarioDTO;
import com.example.demo.usuario.domain.UsuarioResponseDTO;
import com.example.demo.usuario.domain.UsuarioUpdateDTO;
import com.example.demo.usuario.domain.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioService.obtenerTodos();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerActual(@AuthenticationPrincipal UserDetails principal) {
        return usuarioService.obtenerPorEmail(principal.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.esDueñoDeUsuario(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.esDueñoDeUsuario(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return usuarioService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.esDueñoDeUsuario(#id)")
    @PostMapping("/{id}/revocar-sesiones")
    public ResponseEntity<Void> revocarSesiones(@PathVariable Long id) {
        return usuarioService.revocarSesiones(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        usuarioService.eliminar(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
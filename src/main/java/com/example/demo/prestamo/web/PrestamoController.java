package com.example.demo.prestamo.web;

import lombok.RequiredArgsConstructor;


import com.example.demo.prestamo.domain.PrestamoResponseDTO;
import com.example.demo.shared.NotFoundException;
import com.example.demo.prestamo.domain.PrestamoService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<PrestamoResponseDTO> obtenerTodos(){
        return prestamoService.obtenerTodos();
    }

    @GetMapping("/mios")
    public List<PrestamoResponseDTO> obtenerMios(@AuthenticationPrincipal UserDetails principal){
        return prestamoService.obtenerPorEmail(principal.getUsername());
    }

    @PreAuthorize("hasRole('ADMIN') or @prestamoAuthz.esDueño(#id, authentication.name)")
    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponseDTO> obtenerPorId(@PathVariable Long id){
        return prestamoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestParam @Positive Long libroId, @AuthenticationPrincipal UserDetails principal){
        try {
            PrestamoResponseDTO prestamo = prestamoService.crearPrestamo(libroId, principal.getUsername());
            return ResponseEntity.ok(prestamo);
        } catch (NotFoundException e){
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @prestamoAuthz.esDueño(#id, authentication.name)")
    @PutMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(@PathVariable Long id){
        try {
            PrestamoResponseDTO prestamo = prestamoService.devolverLibro(id);
            return ResponseEntity.ok(prestamo);
        } catch (NotFoundException e){
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

}
package com.example.demo.prestamo.internal.web;

import com.example.demo.prestamo.dto.PrestamoResponseDTO;
import com.example.demo.prestamo.internal.domain.PrestamoService;
import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public PrestamoResponseDTO crear(@RequestParam @Positive Long libroId, @AuthenticationPrincipal UserDetails principal){
        return prestamoService.crearPrestamo(libroId, principal.getUsername());
    }

    @PreAuthorize("hasRole('ADMIN') or @prestamoAuthz.esDueño(#id, authentication.name)")
    @PutMapping("/{id}/devolver")
    public PrestamoResponseDTO devolver(@PathVariable Long id){
        return prestamoService.devolverLibro(id);
    }

}

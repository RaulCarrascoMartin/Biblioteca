package com.example.demo.container;


import com.example.demo.domain.dto.PrestamoResponseDTO;
import com.example.demo.domain.service.PrestamoService;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<PrestamoResponseDTO> obtenerTodos(){
        return prestamoService.obtenerTodos()
                .stream()
                .map(PrestamoResponseDTO::new)
                .toList();
    }

    @GetMapping("/mios")
    public List<PrestamoResponseDTO> obtenerMios(@AuthenticationPrincipal UserDetails principal){
        return prestamoService.obtenerPorEmail(principal.getUsername())
                .stream()
                .map(PrestamoResponseDTO::new)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.esDueñoDePrestamo(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponseDTO> obtenerPorId(@PathVariable Long id){
        return prestamoService.obtenerPorId(id)
                .map(PrestamoResponseDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestParam @Positive Long libroId, @AuthenticationPrincipal UserDetails principal){
        try {
            PrestamoResponseDTO prestamo = prestamoService.crearPrestamo(libroId, principal.getUsername());
            return ResponseEntity.ok(prestamo);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.esDueñoDePrestamo(#id)")
    @PutMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(@PathVariable Long id){
        try {
            PrestamoResponseDTO prestamo = prestamoService.devolverLibro(id);
            return ResponseEntity.ok(prestamo);
        }catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }

}
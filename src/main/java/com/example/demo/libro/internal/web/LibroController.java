package com.example.demo.libro.internal.web;

import com.example.demo.libro.dto.LibroDTO;
import com.example.demo.libro.dto.LibroResponseDTO;
import com.example.demo.libro.internal.domain.LibroService;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public List<LibroResponseDTO> obtenerTodos(){
        return libroService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibroResponseDTO> obtenerPorId(@PathVariable Long id){
        return libroService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody LibroDTO dto){
        return ResponseEntity.ok(libroService.crear(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody LibroDTO dto){
        return libroService.actualizar(id, dto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
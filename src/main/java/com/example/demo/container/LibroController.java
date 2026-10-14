package com.example.demo.container;

import lombok.RequiredArgsConstructor;

import com.example.demo.domain.dto.LibroDTO;
import com.example.demo.domain.dto.LibroResponseDTO;
import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public List<LibroResponseDTO> obtenerTodos(){
        return libroService.obtenerTodos()
                .stream()
                .map(LibroResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibroResponseDTO> obtenerPorId(@PathVariable Long id){
        return libroService.obtenerPorId(id)
                .map(LibroResponseDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody LibroDTO dto){
        if (libroService.obtenerPorIsbn(dto.getIsbn()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El ISBN ya existe en el catálogo");
        }

        Libro libro = new Libro();
        libro.setTitulo(dto.getTitulo());
        libro.setAutor(dto.getAutor());
        libro.setIsbn(dto.getIsbn());
        libro.setGenero(dto.getGenero());
        libro.setDisponible(true);

        return ResponseEntity.ok(new LibroResponseDTO(libroService.guardar(libro)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody LibroDTO dto){
        Optional<Libro> existente = libroService.obtenerPorId(id);
        if (existente.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Optional<Libro> duplicado = libroService.obtenerPorIsbn(dto.getIsbn());
        if (duplicado.isPresent() && !duplicado.get().getId().equals(id)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El ISBN ya existe en el catálogo");
        }

        Libro libro = existente.get();
        libro.setTitulo(dto.getTitulo());
        libro.setAutor(dto.getAutor());
        libro.setIsbn(dto.getIsbn());
        if (dto.getGenero() != null) {
            libro.setGenero(dto.getGenero());
        }

        return ResponseEntity.ok(new LibroResponseDTO(libroService.guardar(libro)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
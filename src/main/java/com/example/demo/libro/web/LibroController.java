package com.example.demo.libro.web;

import lombok.RequiredArgsConstructor;

import com.example.demo.libro.domain.LibroDTO;
import com.example.demo.libro.domain.LibroResponseDTO;
import com.example.demo.libro.domain.Libro;
import com.example.demo.libro.domain.LibroService;
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
        if (libroService.obtenerPorIsbn(dto.isbn()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El ISBN ya existe en el catálogo");
        }

        Libro libro = new Libro();
        libro.setTitulo(dto.titulo());
        libro.setAutor(dto.autor());
        libro.setIsbn(dto.isbn());
        libro.setGenero(dto.genero());
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

        Optional<Libro> duplicado = libroService.obtenerPorIsbn(dto.isbn());
        if (duplicado.isPresent() && !duplicado.get().getId().equals(id)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El ISBN ya existe en el catálogo");
        }

        Libro libro = existente.get();
        libro.setTitulo(dto.titulo());
        libro.setAutor(dto.autor());
        libro.setIsbn(dto.isbn());
        if (dto.genero() != null) {
            libro.setGenero(dto.genero());
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
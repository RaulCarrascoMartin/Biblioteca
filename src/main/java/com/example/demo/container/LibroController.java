package com.example.demo.container;

import com.example.demo.domain.dto.LibroDTO;
import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @GetMapping
    public List<Libro> obtenerTodos(){
        return libroService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerPorId(@PathVariable Long id){
        return libroService.obtenerPorId(id)
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

        return ResponseEntity.ok(libroService.guardar(libro));
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

        return ResponseEntity.ok(libroService.guardar(libro));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
package com.example.demo.catalog.internal.controller;

import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.shared.Requester;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Contrato HTTP del catálogo (ruta base {@code /api/libros}). Códigos: 200 / 204 éxito, 400 formato,
 * 401 sin identidad, 403 sin permiso (solo ADMIN crea, edita y borra), 404 libro inexistente,
 * 409 ISBN duplicado o libro con préstamos.
 */
public interface BookHttpApi {

    /** Lista el catálogo. Cualquier usuario autenticado. */
    @GetMapping
    List<BookResponse> getAll(Requester requester);

    /** Obtiene un libro. Cualquier usuario autenticado. 404 si no existe. */
    @GetMapping("/{id}")
    ResponseEntity<BookResponse> getById(Requester requester, @PathVariable Long id);

    /** Crea un libro (ADMIN). 400 formato, 409 ISBN duplicado. */
    @PostMapping
    ResponseEntity<BookResponse> create(Requester requester, @Valid @RequestBody BookRequest request);

    /** Edita un libro (ADMIN). 404 inexistente, 409 ISBN de otro libro. */
    @PutMapping("/{id}")
    ResponseEntity<BookResponse> update(Requester requester, @PathVariable Long id,
                                        @Valid @RequestBody BookRequest request);

    /** Retira un libro (ADMIN). 404 inexistente, 409 con préstamos. */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(Requester requester, @PathVariable Long id);
}

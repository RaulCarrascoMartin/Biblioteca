package com.example.demo.catalog.internal.controller;

import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.shared.Requester;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/** Contrato HTTP del catálogo (ruta base {@code /api/books}). Solo ADMIN crea, edita y borra. */
@Tag(name = "Libros", description = "Catálogo de libros")
@RequestMapping(BookHttpApi.BASE_PATH)
public interface BookHttpApi {

    String BASE_PATH = "/api/books";

    @Operation(summary = "Lista el catálogo", description = "Cualquier usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo completo"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content)
    })
    @GetMapping
    List<BookResponse> getAll(@Parameter(hidden = true) Requester requester);

    @Operation(summary = "Obtiene un libro", description = "Cualquier usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro encontrado"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content),
            @ApiResponse(responseCode = "404", description = "El libro no existe")
    })
    @GetMapping("/{id}")
    ResponseEntity<BookResponse> getById(@Parameter(hidden = true) Requester requester, @PathVariable Long id);

    @Operation(summary = "Crea un libro", description = "Solo ADMIN. Devuelve la cabecera Location del nuevo libro.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Libro creado"),
            @ApiResponse(responseCode = "400", description = "Formato inválido"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "409", description = "ISBN duplicado"),
            @ApiResponse(responseCode = "422", description = "Datos que incumplen una invariante del libro")
    })
    @PostMapping
    ResponseEntity<BookResponse> create(@Parameter(hidden = true) Requester requester,
                                        @Valid @RequestBody BookRequest request);

    @Operation(summary = "Edita un libro", description = "Solo ADMIN. Un género nulo conserva el actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro actualizado"),
            @ApiResponse(responseCode = "400", description = "Formato inválido"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El libro no existe"),
            @ApiResponse(responseCode = "409", description = "El ISBN es de otro libro"),
            @ApiResponse(responseCode = "422", description = "Datos que incumplen una invariante del libro")
    })
    @PutMapping("/{id}")
    ResponseEntity<BookResponse> update(@Parameter(hidden = true) Requester requester, @PathVariable Long id,
                                        @Valid @RequestBody BookRequest request);

    @Operation(summary = "Retira un libro", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Libro retirado"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El libro no existe"),
            @ApiResponse(responseCode = "409", description = "El libro tiene préstamos")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@Parameter(hidden = true) Requester requester, @PathVariable Long id);
}

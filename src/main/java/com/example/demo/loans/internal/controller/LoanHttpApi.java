package com.example.demo.loans.internal.controller;

import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.shared.Requester;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Contrato HTTP de préstamos (ruta base {@code /api/loans}): única fuente de verdad de rutas y validación. */
@Tag(name = "Préstamos", description = "Préstamos de libros")
@RequestMapping(LoanHttpApi.BASE_PATH)
public interface LoanHttpApi {

    String BASE_PATH = "/api/loans";

    @Operation(summary = "Lista todos los préstamos", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todos los préstamos"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permiso")
    })
    @GetMapping
    List<LoanResponse> getAll(@Parameter(hidden = true) Requester requester);

    @Operation(summary = "Lista mis préstamos", description = "Cualquier usuario autenticado: sus propios préstamos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamos del solicitante"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content)
    })
    @GetMapping("/mine")
    List<LoanResponse> getOwn(@Parameter(hidden = true) Requester requester);

    @Operation(summary = "Obtiene un préstamo", description = "ADMIN o dueño del préstamo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo encontrado"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El préstamo no existe")
    })
    @GetMapping("/{id}")
    LoanResponse getById(@Parameter(hidden = true) Requester requester, @PathVariable("id") Long id);

    @Operation(summary = "Presta un libro",
            description = "Cualquier usuario autenticado: presta el libro a nombre del solicitante. "
                    + "Devuelve la cabecera Location del nuevo préstamo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Préstamo creado"),
            @ApiResponse(responseCode = "400", description = "bookId ausente o no positivo"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El libro o el usuario no existen"),
            @ApiResponse(responseCode = "409", description = "El libro no está disponible"),
            @ApiResponse(responseCode = "422", description = "Datos que incumplen una invariante del préstamo")
    })
    @PostMapping
    ResponseEntity<LoanResponse> create(@Parameter(hidden = true) Requester requester,
                                        @RequestParam("bookId") @Positive Long bookId);

    @Operation(summary = "Devuelve un libro", description = "ADMIN o dueño del préstamo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo devuelto"),
            @ApiResponse(responseCode = "401", description = "Sin identidad", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "404", description = "El préstamo no existe"),
            @ApiResponse(responseCode = "409",
                    description = "Ya estaba devuelto, el libro ya estaba disponible o hubo una modificación concurrente")
    })
    @PutMapping("/{id}/return")
    LoanResponse returnLoan(@Parameter(hidden = true) Requester requester, @PathVariable("id") Long id);
}

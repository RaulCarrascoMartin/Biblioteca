package com.example.demo.loans.internal.controller;

import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.shared.Requester;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrato HTTP de préstamos (única fuente de verdad de rutas y validación). Códigos: 200 éxito;
 * 400 formato (libroId no positivo o ausente); 401 sin sesión; 403 sin permiso; 404 préstamo, libro o
 * usuario inexistente; 409 libro no disponible o préstamo ya devuelto.
 */
@RequestMapping("/api/prestamos")
public interface LoanHttpApi {

    /** Solo ADMIN. */
    @GetMapping
    List<LoanResponse> getAll(Requester requester);

    /** Cualquier autenticado: sus propios préstamos. */
    @GetMapping("/mios")
    List<LoanResponse> getOwn(Requester requester);

    /** ADMIN o dueño del préstamo. 404 si no existe. */
    @GetMapping("/{id}")
    LoanResponse getById(Requester requester, @PathVariable("id") Long id);

    /** Cualquier autenticado: presta el libro a nombre del solicitante. 404 libro inexistente, 409 no disponible. */
    @PostMapping
    LoanResponse create(Requester requester, @RequestParam("libroId") @Positive Long bookId);

    /** ADMIN o dueño del préstamo. 404 inexistente, 409 ya devuelto. */
    @PutMapping("/{id}/devolver")
    LoanResponse returnLoan(Requester requester, @PathVariable("id") Long id);
}

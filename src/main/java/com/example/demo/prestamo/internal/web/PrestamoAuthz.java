package com.example.demo.prestamo.internal.web;

import com.example.demo.prestamo.internal.domain.PrestamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Bean para expresiones {@code @PreAuthorize}: «¿es este usuario el dueño del préstamo?». */
@Component("prestamoAuthz")
@RequiredArgsConstructor
public class PrestamoAuthz {

    private final PrestamoService prestamoService;

    public boolean esDueño(Long id, String email) {
        return prestamoService.esDueño(id, email);
    }
}

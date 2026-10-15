package com.example.demo.prestamo.web;

import com.example.demo.prestamo.domain.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Bean para expresiones {@code @PreAuthorize}: «¿es este usuario el dueño del préstamo?». */
@Component("prestamoAuthz")
@RequiredArgsConstructor
public class PrestamoAuthz {

    private final PrestamoRepository prestamoRepository;

    public boolean esDueño(Long id, String email) {
        if (email == null) {
            return false;
        }
        return prestamoRepository.findById(id)
                .map(prestamo -> email.equalsIgnoreCase(prestamo.getUsuario().getEmail()))
                .orElse(false);
    }
}

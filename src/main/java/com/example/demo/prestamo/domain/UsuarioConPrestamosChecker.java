package com.example.demo.prestamo.domain;

import com.example.demo.usuario.domain.UsuarioEnUsoChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Un usuario con préstamos registrados no puede eliminarse (se conserva el historial). */
@Component
@RequiredArgsConstructor
public class UsuarioConPrestamosChecker implements UsuarioEnUsoChecker {

    private final PrestamoRepository prestamoRepository;

    @Override
    public boolean estaEnUso(Long usuarioId) {
        return prestamoRepository.existsByUsuarioId(usuarioId);
    }
}

package com.example.demo.security;

import com.example.demo.persistance.repository.PrestamoRepository;
import com.example.demo.persistance.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthzHelper {

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;

    public AuthzHelper(UsuarioRepository usuarioRepository, PrestamoRepository prestamoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public String emailActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    public boolean esDueñoDeUsuario(Long id) {
        String email = emailActual();
        if (email == null) {
            return false;
        }
        return usuarioRepository.findById(id)
                .map(usuario -> email.equalsIgnoreCase(usuario.getEmail()))
                .orElse(false);
    }

    public boolean esDueñoDePrestamo(Long id) {
        String email = emailActual();
        if (email == null) {
            return false;
        }
        return prestamoRepository.findById(id)
                .map(prestamo -> email.equalsIgnoreCase(prestamo.getUsuario().getEmail()))
                .orElse(false);
    }
}
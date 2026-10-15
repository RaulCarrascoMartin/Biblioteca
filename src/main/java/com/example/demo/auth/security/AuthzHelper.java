package com.example.demo.auth.security;

import com.example.demo.usuario.domain.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthzHelper {

    private final UsuarioRepository usuarioRepository;

    public AuthzHelper(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
}

package com.example.demo.auth.internal.security;

import com.example.demo.usuario.UsuarioModule;
import com.example.demo.usuario.dto.UsuarioCredencialesDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioModule usuarioModule;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioCredencialesDTO usuario = usuarioModule.obtenerCredenciales(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));

        return new AuthUser(
                usuario.email(),
                usuario.password(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())),
                usuario.tokenVersion()
        );
    }
}

package com.example.demo.support;

import com.example.demo.shared.AuthenticatedPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Identidad de prueba para los tests de controller con la {@code SecurityConfig} real: publica el mismo
 * {@code AuthenticatedPrincipal} que el filtro JWT (id y roles sin prefijo; aquí se añade {@code ROLE_}).
 */
public final class TestAuthentication {

    private TestAuthentication() {
    }

    public static RequestPostProcessor asUser(Long userId, String... roles) {
        List<String> prefixed = Arrays.stream(roles).map(r -> "ROLE_" + r).toList();
        var authorities = prefixed.stream().map(SimpleGrantedAuthority::new).toList();
        return authentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedPrincipal(userId, prefixed), null, authorities));
    }
}

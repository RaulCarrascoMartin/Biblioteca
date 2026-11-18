package com.example.demo.config.web;

import com.example.demo.shared.AuthenticatedPrincipal;
import com.example.demo.shared.Requester;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Construye el {@link Requester} desde el {@code SecurityContext} para que los controllers lo pasen
 * a los casos de uso sin importar Spring Security. Usa solo el {@link AuthenticatedPrincipal} del token
 * (id y roles, sin el prefijo {@code ROLE_}); no consulta la base de datos.
 */
@Component
public class RequesterArgumentResolver implements HandlerMethodArgumentResolver {

    static final String ROLE_PREFIX = "ROLE_";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Requester.class.equals(parameter.getParameterType());
    }

    @Override
    public Requester resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            // Se traduce a 401; SecurityConfig ya bloquea las rutas sin sesión, esto es una red de seguridad
            throw new AuthenticationCredentialsNotFoundException("No autenticado");
        }
        Set<String> roles = principal.roles().stream()
                .map(r -> r.startsWith(ROLE_PREFIX) ? r.substring(ROLE_PREFIX.length()) : r)
                .collect(Collectors.toSet());
        return new Requester(principal.userId(), roles);
    }
}

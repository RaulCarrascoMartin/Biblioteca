package com.example.demo.config;

import com.example.demo.shared.Requester;
import com.example.demo.users.UsersModule;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Construye el {@link Requester} desde el {@code SecurityContext} para que los controllers lo pasen
 * a los casos de uso sin importar Spring Security. Los roles se entregan sin el prefijo {@code ROLE_}.
 * El id se resuelve por email con la API pública de {@code users}.
 */
@Component
public class RequesterArgumentResolver implements HandlerMethodArgumentResolver {

    static final String ROLE_PREFIX = "ROLE_";

    private final UsersModule usersModule;

    public RequesterArgumentResolver(UsersModule usersModule) {
        this.usersModule = usersModule;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Requester.class.equals(parameter.getParameterType());
    }

    @Override
    public Requester resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails principal)) {
            // Se traduce a 401; SecurityConfig ya bloquea las rutas sin sesión, esto es una red de seguridad
            throw new AuthenticationCredentialsNotFoundException("No autenticado");
        }
        Long id = usersModule.getByEmail(principal.getUsername())
                .map(user -> user.id())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("No autenticado"));
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith(ROLE_PREFIX) ? a.substring(ROLE_PREFIX.length()) : a)
                .collect(Collectors.toSet());
        return new Requester(id, roles);
    }
}

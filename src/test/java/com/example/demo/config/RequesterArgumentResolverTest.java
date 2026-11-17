package com.example.demo.config;

import com.example.demo.shared.Requester;
import com.example.demo.shared.AuthenticatedPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequesterArgumentResolverTest {

    private final RequesterArgumentResolver resolver = new RequesterArgumentResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unused")
    private void handler(Requester requester, String other) {
    }

    private MethodParameter parameter(int index) throws Exception {
        Method method = RequesterArgumentResolverTest.class.getDeclaredMethod("handler", Requester.class, String.class);
        return new MethodParameter(method, index);
    }

    private void authenticate(Object principal, String... authorities) {
        List<SimpleGrantedAuthority> granted = java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, granted));
    }

    @Test
    void supportsOnlyRequesterParameters() throws Exception {
        assertTrue(resolver.supportsParameter(parameter(0)));
        assertFalse(resolver.supportsParameter(parameter(1)));
    }

    @Test
    void buildsRequesterFromThePrincipalWithRolesWithoutPrefix() throws Exception {
        authenticate(new AuthenticatedPrincipal(7L, List.of("ROLE_ADMIN", "ROLE_USER")), "ROLE_ADMIN");

        Requester requester = resolver.resolveArgument(parameter(0), null, null, null);

        assertEquals(new Requester(7L, Set.of("ADMIN", "USER")), requester);
        assertTrue(requester.isAdmin());
        assertTrue(requester.isOwner(7L));
    }

    @Test
    void withoutAuthenticationItFailsAsUnauthenticated() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> resolver.resolveArgument(parameter(0), null, null, null));
    }

    @Test
    void aPrincipalThatIsNotFromTheTokenFailsAsUnauthenticated() {
        authenticate("ana@test.es", "ROLE_USER");

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> resolver.resolveArgument(parameter(0), null, null, null));
    }
}

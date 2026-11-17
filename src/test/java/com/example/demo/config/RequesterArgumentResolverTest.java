package com.example.demo.config;

import com.example.demo.shared.Requester;
import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequesterArgumentResolverTest {

    @Mock
    private UsersModule usersModule;

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

    private void authenticate(String email, String... authorities) {
        List<SimpleGrantedAuthority> granted = java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList();
        User principal = new User(email, "x", granted);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, granted));
    }

    @Test
    void supportsOnlyRequesterParameters() throws Exception {
        RequesterArgumentResolver resolver = new RequesterArgumentResolver(usersModule);
        assertTrue(resolver.supportsParameter(parameter(0)));
        assertFalse(resolver.supportsParameter(parameter(1)));
    }

    @Test
    void buildsRequesterWithIdAndRolesWithoutPrefix() throws Exception {
        authenticate("ana@test.es", "ROLE_ADMIN");
        when(usersModule.getByEmail("ana@test.es"))
                .thenReturn(Optional.of(new UserResponseDto(7L, "Ana", "ana@test.es", "ADMIN")));

        Requester requester = new RequesterArgumentResolver(usersModule).resolveArgument(parameter(0), null, null, null);

        assertEquals(new Requester(7L, Set.of("ADMIN")), requester);
        assertTrue(requester.isAdmin());
        assertTrue(requester.isOwner(7L));
    }

    @Test
    void withoutAuthenticationItFailsAsUnauthenticated() {
        RequesterArgumentResolver resolver = new RequesterArgumentResolver(usersModule);
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> resolver.resolveArgument(parameter(0), null, null, null));
    }

    @Test
    void unknownUserFailsAsUnauthenticated() {
        authenticate("fantasma@test.es", "ROLE_USER");
        when(usersModule.getByEmail("fantasma@test.es")).thenReturn(Optional.empty());
        RequesterArgumentResolver resolver = new RequesterArgumentResolver(usersModule);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> resolver.resolveArgument(parameter(0), null, null, null));
    }
}

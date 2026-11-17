package com.example.demo.config;

import com.example.demo.shared.TokenClaims;
import com.example.demo.shared.TokenProvider;
import com.example.demo.shared.TokenRevocation;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private TokenRevocation tokenRevocation;
    @Mock
    private CustomUserDetailsService userDetailsService;

    private JwtAuthFilter filter;
    private MockHttpServletRequest request;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(tokenProvider, tokenRevocation, userDetailsService);
        request = new MockHttpServletRequest();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private AuthUser user(int version) {
        return new AuthUser("a@test.es", "hash", List.of(new SimpleGrantedAuthority("ROLE_USER")), version);
    }

    private void run() throws Exception {
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest(), "la petición debe continuar la cadena");
    }

    @Test
    void validBearerTokenAuthenticates() throws Exception {
        request.addHeader("Authorization", "Bearer tok");
        when(tokenProvider.validate("tok")).thenReturn(Optional.of(new TokenClaims("a@test.es", 2)));
        when(userDetailsService.loadUserByUsername("a@test.es")).thenReturn(user(2));

        run();

        assertEquals("a@test.es", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void validCookieTokenAuthenticates() throws Exception {
        request.setCookies(new Cookie("jwt_token", "tok"));
        when(tokenProvider.validate("tok")).thenReturn(Optional.of(new TokenClaims("a@test.es", 0)));
        when(userDetailsService.loadUserByUsername("a@test.es")).thenReturn(user(0));

        run();

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void noTokenLeavesRequestAnonymous() throws Exception {
        run();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void revokedTokenDoesNotAuthenticate() throws Exception {
        request.addHeader("Authorization", "Bearer tok");
        when(tokenRevocation.isRevoked("tok")).thenReturn(true);

        run();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidTokenDoesNotAuthenticate() throws Exception {
        request.addHeader("Authorization", "Bearer tok");
        when(tokenProvider.validate("tok")).thenReturn(Optional.empty());

        run();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void staleTokenVersionDoesNotAuthenticate() throws Exception {
        request.addHeader("Authorization", "Bearer tok");
        when(tokenProvider.validate("tok")).thenReturn(Optional.of(new TokenClaims("a@test.es", 1)));
        when(userDetailsService.loadUserByUsername("a@test.es")).thenReturn(user(2));

        run();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deletedUserDoesNotAuthenticateNorBreakTheChain() throws Exception {
        request.addHeader("Authorization", "Bearer tok");
        when(tokenProvider.validate("tok")).thenReturn(Optional.of(new TokenClaims("a@test.es", 0)));
        when(userDetailsService.loadUserByUsername("a@test.es")).thenThrow(new UsernameNotFoundException("x"));

        run();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

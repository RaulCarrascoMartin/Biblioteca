package com.example.demo.config;

import com.example.demo.shared.AuthenticatedPrincipal;
import com.example.demo.shared.TokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenProvider);
        request = new MockHttpServletRequest();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void run() throws Exception {
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest(), "la petición debe continuar la cadena");
    }

    @Test
    void validBearerTokenPublishesThePrincipalWithRolesAsAuthorities() throws Exception {
        request.addHeader("Authorization", "Bearer tok");
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(5L, List.of("ROLE_ADMIN"));
        when(tokenProvider.validate("tok")).thenReturn(Optional.of(principal));

        run();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(principal, authentication.getPrincipal());
        assertEquals("ROLE_ADMIN", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void noTokenLeavesRequestAnonymous() throws Exception {
        run();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void invalidTokenDoesNotAuthenticate() throws Exception {
        request.addHeader("Authorization", "Bearer tok");
        when(tokenProvider.validate("tok")).thenReturn(Optional.empty());

        run();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void nonBearerSchemeIsIgnored() throws Exception {
        request.addHeader("Authorization", "Basic abc");

        run();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void blankBearerIsIgnored() throws Exception {
        request.addHeader("Authorization", "Bearer   ");

        run();

        verifyNoInteractions(tokenProvider);
    }

    @Test
    void theOldJwtCookieIsNoLongerAccepted() throws Exception {
        request.setCookies(new Cookie("jwt_token", "tok"));

        run();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider);
    }
}

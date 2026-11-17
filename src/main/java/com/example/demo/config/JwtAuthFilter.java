package com.example.demo.config;

import com.example.demo.shared.TokenClaims;
import com.example.demo.shared.TokenProvider;
import com.example.demo.shared.TokenRevocation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/** Autentica la petición a partir del token (cabecera Bearer o cookie {@code jwt_token}). */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String COOKIE = "jwt_token";
    private static final String BEARER = "Bearer ";

    private final TokenProvider tokenProvider;
    private final TokenRevocation tokenRevocation;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(TokenProvider tokenProvider, TokenRevocation tokenRevocation,
                         CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.tokenRevocation = tokenRevocation;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && !token.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                authenticate(request, token);
            } catch (Exception e) {
                // Token inválido o usuario inexistente: no se autentica y Spring Security responde 401 si la ruta lo exige
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        if (tokenRevocation.isRevoked(token)) {
            return;
        }
        Optional<TokenClaims> claims = tokenProvider.validate(token);
        if (claims.isEmpty()) {
            return;
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.get().email());
        int currentVersion = userDetails instanceof AuthUser au ? au.getTokenVersion() : 0;
        if (claims.get().version() != currentVersion) {
            return;
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private static String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER)) {
            return authHeader.substring(BEARER.length());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

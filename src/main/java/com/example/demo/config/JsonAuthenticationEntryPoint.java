package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/** 401 en JSON (mismo formato que el resto de errores: {@code detail} y {@code error}) sin detalles internos. */
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String MESSAGE = "No autenticado";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":401,\"title\":\"Unauthorized\",\"detail\":\"" + MESSAGE
                + "\",\"error\":\"" + MESSAGE + "\"}");
    }
}

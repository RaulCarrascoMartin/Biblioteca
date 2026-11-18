package com.example.demo.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/** 403 en JSON (mismo formato que el resto de errores) cuando hay identidad pero no permiso. */
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    static final String MESSAGE = "No tiene permisos para acceder a este recurso";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":403,\"title\":\"Forbidden\",\"detail\":\"" + MESSAGE
                + "\",\"error\":\"" + MESSAGE + "\"}");
    }
}

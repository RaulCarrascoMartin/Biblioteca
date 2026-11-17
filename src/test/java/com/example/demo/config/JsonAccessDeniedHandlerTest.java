package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAccessDeniedHandlerTest {

    @Test
    void respondsJson403WithoutInternalDetails() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAccessDeniedHandler().handle(new MockHttpServletRequest(), response,
                new AccessDeniedException("detalle interno"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/problem+json"));
        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":403"));
        assertTrue(body.contains("\"error\":\"No tiene permisos para acceder a este recurso\""));
        assertFalse(body.contains("detalle interno"));
    }
}

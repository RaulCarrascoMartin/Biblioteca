package com.example.demo.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAuthenticationEntryPointTest {

    @Test
    void respondsJson401WithoutInternalDetails() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAuthenticationEntryPoint().commence(new MockHttpServletRequest(), response,
                new BadCredentialsException("detalle interno"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/problem+json"));
        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":401"));
        assertTrue(body.contains("\"error\":\"No autenticado\""));
        assertTrue(body.contains("\"detail\":\"No autenticado\""));
        assertFalse(body.contains("detalle interno"));
    }
}

package com.example.demo.shared;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticatedPrincipalTest {

    @Test
    void nullUserIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AuthenticatedPrincipal(null, List.of("ROLE_USER")));
    }

    @Test
    void nullRolesBecomeEmpty() {
        assertTrue(new AuthenticatedPrincipal(1L, null).roles().isEmpty());
    }

    @Test
    void rolesAreCopiedDefensively() {
        List<String> roles = new ArrayList<>(List.of("ROLE_USER"));
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(1L, roles);
        roles.add("ROLE_ADMIN");

        assertEquals(List.of("ROLE_USER"), principal.roles());
    }
}

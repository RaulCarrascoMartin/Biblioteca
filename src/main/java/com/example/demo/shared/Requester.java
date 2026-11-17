package com.example.demo.shared;

import java.util.Set;

/** Quién inicia un flujo de usuario: lo que necesitan los casos de uso para comprobar la autorización. */
public record Requester(Long id, Set<String> roles) {

    private static final String ADMIN = "ADMIN";

    public Requester {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public boolean isAdmin() {
        return roles.contains(ADMIN);
    }

    public boolean isOwner(Long id) {
        return this.id != null && this.id.equals(id);
    }
}

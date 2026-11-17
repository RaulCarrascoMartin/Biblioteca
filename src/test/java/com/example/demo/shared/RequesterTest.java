package com.example.demo.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RequesterTest {

    @Test
    void isAdminTrueWhenRolesContainAdmin() {
        assertTrue(new Requester(1L, Set.of("USER", "ADMIN")).isAdmin());
    }

    @Test
    void isAdminFalseWithoutAdminRole() {
        assertFalse(new Requester(1L, Set.of("USER")).isAdmin());
    }

    @Test
    void nullRolesBecomeEmpty() {
        Requester r = new Requester(1L, null);
        assertTrue(r.roles().isEmpty());
        assertFalse(r.isAdmin());
    }

    @Test
    void isOwnerComparesIds() {
        Requester r = new Requester(5L, Set.of());
        assertTrue(r.isOwner(5L));
        assertFalse(r.isOwner(6L));
        assertFalse(r.isOwner(null));
    }

    @Test
    void isOwnerFalseWhenOwnIdIsNull() {
        Requester r = new Requester(null, Set.of());
        assertFalse(r.isOwner(null));
        assertFalse(r.isOwner(1L));
    }

    @Test
    void rolesAreDefensivelyCopiedAndImmutable() {
        Set<String> original = new HashSet<>(Set.of("USER"));
        Requester r = new Requester(1L, original);
        original.add("ADMIN");
        assertFalse(r.isAdmin());
        assertThrows(UnsupportedOperationException.class, () -> r.roles().add("ADMIN"));
    }
}

package com.example.demo.users.internal.domain.model;

import com.example.demo.users.internal.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    private static final Email EMAIL = new Email("ana@x.es");

    private static User ana() {
        return new User(1L, "Ana", EMAIL, "hash", Role.USER, 0);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsBlankName(String name) {
        assertThrows(InvalidUserException.class, () -> new User(1L, name, EMAIL, "hash", Role.USER, 0));
    }

    @Test
    void nameLengthBoundary() {
        assertEquals(User.NAME_MAX_LENGTH,
                new User(1L, "a".repeat(User.NAME_MAX_LENGTH), EMAIL, "hash", Role.USER, 0).name().length());
        assertThrows(InvalidUserException.class,
                () -> new User(1L, "a".repeat(User.NAME_MAX_LENGTH + 1), EMAIL, "hash", Role.USER, 0));
    }

    @Test
    void trimsName() {
        assertEquals("Ana", new User(1L, "  Ana ", EMAIL, "hash", Role.USER, 0).name());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void rejectsBlankPasswordHash(String hash) {
        assertThrows(InvalidUserException.class, () -> new User(1L, "Ana", EMAIL, hash, Role.USER, 0));
    }

    @Test
    void rejectsNullEmailAndRole() {
        assertThrows(InvalidUserException.class, () -> new User(1L, "Ana", null, "hash", Role.USER, 0));
        assertThrows(InvalidUserException.class, () -> new User(1L, "Ana", EMAIL, "hash", null, 0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    void rejectsNegativeTokenVersion(int version) {
        assertThrows(InvalidUserException.class, () -> new User(1L, "Ana", EMAIL, "hash", Role.USER, version));
    }

    @Test
    void zeroTokenVersionIsValid() {
        assertEquals(0, ana().tokenVersion());
    }

    @Test
    void newUserHasUserRoleNoIdAndVersionZero() {
        User user = User.newUser("Ana", EMAIL, "hash");

        assertNull(user.id());
        assertEquals(Role.USER, user.role());
        assertEquals(0, user.tokenVersion());
    }

    @Test
    void newAdminHasAdminRole() {
        User admin = User.newAdmin(EMAIL, "hash");

        assertNull(admin.id());
        assertEquals(Role.ADMIN, admin.role());
        assertEquals("Administrador", admin.name());
    }

    @Test
    void renameAndChangeEmailReturnNewUserKeepingTheRest() {
        User renamed = ana().rename(" Ana M ");
        User changed = ana().changeEmail(new Email("otra@x.es"));

        assertEquals("Ana M", renamed.name());
        assertEquals(EMAIL, renamed.email());
        assertEquals("otra@x.es", changed.email().value());
        assertEquals("Ana", changed.name());
        assertEquals(0, changed.tokenVersion());
        assertEquals("Ana", ana().name());
    }

    @Test
    void renameRejectsBlank() {
        assertThrows(InvalidUserException.class, () -> ana().rename(" "));
    }

    @Test
    void changePasswordBumpsTokenVersion() {
        User changed = ana().changePassword("nuevo");

        assertEquals("nuevo", changed.passwordHash());
        assertEquals(1, changed.tokenVersion());
        assertEquals("hash", ana().passwordHash());
    }

    @Test
    void revokeSessionsBumpsTokenVersionOnly() {
        User revoked = ana().revokeSessions().revokeSessions();

        assertEquals(2, revoked.tokenVersion());
        assertEquals("hash", revoked.passwordHash());
    }

    @Test
    void toStringHidesPasswordHash() {
        assertFalse(ana().toString().contains("hash"));
    }
}

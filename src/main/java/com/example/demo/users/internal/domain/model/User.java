package com.example.demo.users.internal.domain.model;

import com.example.demo.users.internal.domain.exception.InvalidUserException;

/**
 * Usuario del sistema. Inmutable: cada operación devuelve un usuario nuevo. {@code passwordHash} es
 * siempre el hash, nunca la contraseña en claro. {@code tokenVersion} sube para invalidar los tokens
 * emitidos antes (cambio de clave, cierre global de sesiones).
 */
public record User(Long id, String name, Email email, String passwordHash, Role role, int tokenVersion) {

    public static final int NAME_MAX_LENGTH = 100;
    private static final String ADMIN_NAME = "Administrador";

    public User {
        if (name == null || name.isBlank()) {
            throw new InvalidUserException("El nombre es obligatorio");
        }
        name = name.trim();
        if (name.length() > NAME_MAX_LENGTH) {
            throw new InvalidUserException("El nombre no puede superar %d caracteres".formatted(NAME_MAX_LENGTH));
        }
        if (email == null) {
            throw new InvalidUserException("El email es obligatorio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidUserException("El hash de la contraseña es obligatorio");
        }
        if (role == null) {
            throw new InvalidUserException("El rol es obligatorio");
        }
        if (tokenVersion < 0) {
            throw new InvalidUserException("La versión del token no puede ser negativa");
        }
    }

    /** Usuario nuevo con rol USER, sin id. */
    public static User newUser(String name, Email email, String passwordHash) {
        return new User(null, name, email, passwordHash, Role.USER, 0);
    }

    /** Administrador nuevo, sin id. */
    public static User newAdmin(Email email, String passwordHash) {
        return new User(null, ADMIN_NAME, email, passwordHash, Role.ADMIN, 0);
    }

    public User rename(String newName) {
        return new User(id, newName, email, passwordHash, role, tokenVersion);
    }

    public User changeEmail(Email newEmail) {
        return new User(id, name, newEmail, passwordHash, role, tokenVersion);
    }

    /** Cambia el hash e invalida los tokens anteriores. */
    public User changePassword(String newPasswordHash) {
        return new User(id, name, email, newPasswordHash, role, tokenVersion + 1);
    }

    /** Invalida todos los tokens emitidos hasta ahora. */
    public User revokeSessions() {
        return new User(id, name, email, passwordHash, role, tokenVersion + 1);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email=" + email.value() + ", role=" + role + "}";
    }
}

package com.example.demo.shared;

/** Puerto técnico: la tecnología de hash (BCrypt) vive en {@code config}; el negocio solo usa esta interfaz. */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}

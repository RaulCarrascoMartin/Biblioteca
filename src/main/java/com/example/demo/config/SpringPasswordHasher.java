package com.example.demo.config;

import com.example.demo.shared.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Implementación técnica de {@link PasswordHasher} con el {@link PasswordEncoder} (BCrypt) de Spring. */
@Component
public class SpringPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public SpringPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return passwordEncoder.matches(rawPassword, hash);
    }
}

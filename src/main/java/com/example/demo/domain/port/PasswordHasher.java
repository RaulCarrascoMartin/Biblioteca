package com.example.demo.domain.port;

public interface PasswordHasher {
    String encode(String rawPassword);
}

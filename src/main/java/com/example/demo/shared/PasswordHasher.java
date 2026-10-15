package com.example.demo.shared;

public interface PasswordHasher {
    String encode(String rawPassword);
}

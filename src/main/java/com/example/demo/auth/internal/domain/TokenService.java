package com.example.demo.auth.internal.domain;

public interface TokenService {
    String generarToken(String email, int version);
}

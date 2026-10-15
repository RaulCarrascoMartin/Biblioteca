package com.example.demo.auth.domain;

public interface TokenService {
    String generarToken(String email, int version);
}

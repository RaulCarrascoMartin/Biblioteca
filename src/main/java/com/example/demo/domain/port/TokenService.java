package com.example.demo.domain.port;

public interface TokenService {
    String generarToken(String email, int version);
}

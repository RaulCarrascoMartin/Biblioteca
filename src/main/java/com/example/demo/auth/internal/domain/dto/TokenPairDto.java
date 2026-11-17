package com.example.demo.auth.internal.domain.dto;

/** Par de tokens: JWT de acceso (corto) y refresh opaco (largo, solo en claro al emitirlo). */
public record TokenPairDto(String accessToken, String refreshToken) {

    @Override
    public String toString() {
        return "TokenPairDto{accessToken=***, refreshToken=***}";
    }
}

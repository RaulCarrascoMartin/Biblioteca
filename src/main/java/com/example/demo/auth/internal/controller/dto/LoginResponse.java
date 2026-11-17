package com.example.demo.auth.internal.controller.dto;

/** Par de tokens (login y refresh): el acceso viaja como {@code Authorization: Bearer}. */
public record LoginResponse(String accessToken, String refreshToken) {

    @Override
    public String toString() {
        return "LoginResponse{accessToken=***, refreshToken=***}";
    }
}

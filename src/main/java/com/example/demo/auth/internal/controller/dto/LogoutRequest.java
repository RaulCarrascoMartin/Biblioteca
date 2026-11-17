package com.example.demo.auth.internal.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogoutRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        @Size(max = 512, message = "El refresh token es demasiado largo")
        String refreshToken
) {
}

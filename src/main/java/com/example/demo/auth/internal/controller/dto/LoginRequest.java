package com.example.demo.auth.internal.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
        @Size(max = 255, message = "El email es demasiado largo")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72, message = "La contraseña no puede superar 72 caracteres")
        String password
) {
}

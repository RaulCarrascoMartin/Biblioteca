package com.example.demo.libro.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LibroDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título es demasiado largo")
        String titulo,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 255, message = "El autor es demasiado largo")
        String autor,

        @NotBlank(message = "El ISBN es obligatorio")
        @Size(min = 10, max = 20, message = "El ISBN debe tener entre 10 y 20 caracteres")
        String isbn,

        Genero genero
) {
}

package com.example.demo.catalog.internal.controller.dto;

import com.example.demo.catalog.dto.Genre;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO web de entrada para crear o actualizar un libro: aquí vive la validación de formato (Jakarta).
 * Los nombres JSON se mantienen (contrato HTTP sin cambios).
 */
public record BookRequest(
        @JsonProperty("titulo")
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título es demasiado largo")
        String title,

        @JsonProperty("autor")
        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 255, message = "El autor es demasiado largo")
        String author,

        @JsonProperty("isbn")
        @NotBlank(message = "El ISBN es obligatorio")
        @Size(min = 10, max = 20, message = "El ISBN debe tener entre 10 y 20 caracteres")
        String isbn,

        @JsonProperty("genero")
        Genre genre
) {
}

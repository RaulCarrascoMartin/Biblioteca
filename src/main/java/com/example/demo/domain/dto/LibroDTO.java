package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Genero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LibroDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título es demasiado largo")
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 255, message = "El autor es demasiado largo")
    private String autor;

    @NotBlank(message = "El ISBN es obligatorio")
    @Size(min = 10, max = 20, message = "El ISBN debe tener entre 10 y 20 caracteres")
    private String isbn;

    private Genero genero;
}
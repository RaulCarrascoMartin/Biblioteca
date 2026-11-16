package com.example.demo.libro.internal.web;

import com.example.demo.libro.dto.Genero;
import com.example.demo.libro.internal.persistence.LibroEntity;
import com.example.demo.usuario.internal.persistence.UsuarioEntity;
import com.example.demo.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LibroControllerTest extends BaseIntegrationTest {

    private UsuarioEntity admin;
    private UsuarioEntity user;
    private LibroEntity libro;

    @BeforeEach
    void setUp() {
        limpiarBD();
        admin = crearUsuario("Admin", "ladmin@test.es", "ADMIN");
        user = crearUsuario("Lector", "luser@test.es", "USER");
        libro = crearLibro("Libro Existente");
    }

    @Test
    void listarSinToken_Devuelve401() throws Exception {
        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarConUsuario_Devuelve200() throws Exception {
        mockMvc.perform(get("/api/libros").header("Authorization", "Bearer " + tokenDe(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void obtenerPorId_LibroExistente_200() throws Exception {
        mockMvc.perform(get("/api/libros/" + libro.getId())
                        .header("Authorization", "Bearer " + tokenDe(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value(libro.getTitulo()));
    }

    @Test
    void obtenerPorId_LibroInexistente_404() throws Exception {
        mockMvc.perform(get("/api/libros/9999")
                        .header("Authorization", "Bearer " + tokenDe(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_comoAdmin_CreaElLibro() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Nuevo Libro", "autor", "Autor Nuevo", "isbn", "978000000010"));

        mockMvc.perform(post("/api/libros")
                        .header("Authorization", "Bearer " + tokenDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.isbn").value("978000000010"));

        assertTrue(libroRepository.findByIsbn("978000000010").isPresent());
    }

    @Test
    void crear_conGenero_GuardaElGenero() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Crimen en la nieve", "autor", "Agatha Hélice", "isbn", "978000000013",
                "genero", "MISTERIO"));

        mockMvc.perform(post("/api/libros")
                        .header("Authorization", "Bearer " + tokenDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genero").value("MISTERIO"));

        assertEquals(Genero.MISTERIO,
                libroRepository.findByIsbn("978000000013").orElseThrow().getGenero());
    }

    @Test
    void crear_comoUser_Devuelve403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Hack", "autor", "H", "isbn", "978000000011"));
        mockMvc.perform(post("/api/libros")
                        .header("Authorization", "Bearer " + tokenDe(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_isbnDuplicado_Devuelve409() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Dup", "autor", "D", "isbn", libro.getIsbn()));
        mockMvc.perform(post("/api/libros")
                        .header("Authorization", "Bearer " + tokenDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().string("El ISBN ya existe en el catálogo"));
    }

    @Test
    void crear_camposVacios_Devuelve400() throws Exception {
        mockMvc.perform(post("/api/libros")
                        .header("Authorization", "Bearer " + tokenDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        org.hamcrest.Matchers.containsString("título")));
    }

    @Test
    void actualizar_comoAdmin_CambiaElTitulo() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Libro Modificado", "autor", libro.getAutor(), "isbn", libro.getIsbn()));

        mockMvc.perform(put("/api/libros/" + libro.getId())
                        .header("Authorization", "Bearer " + tokenDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Libro Modificado"));

        assertTrue(libroRepository.findById(libro.getId()).orElseThrow().isDisponible());
    }

    @Test
    void actualizar_isbnQuePerteneceAOtro_Devuelve409() throws Exception {
        LibroEntity otro = crearLibro("Otro");
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "X", "autor", "X", "isbn", libro.getIsbn()));

        mockMvc.perform(put("/api/libros/" + otro.getId())
                        .header("Authorization", "Bearer " + tokenDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizar_LibroInexistente_404() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "X", "autor", "X", "isbn", "978000000012"));
        mockMvc.perform(put("/api/libros/9999")
                        .header("Authorization", "Bearer " + tokenDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_comoAdmin_204LuegoNoExiste() throws Exception {
        mockMvc.perform(delete("/api/libros/" + libro.getId())
                        .header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/libros/" + libro.getId())
                        .header("Authorization", "Bearer " + tokenDe(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_comoUser_Devuelve403() throws Exception {
        mockMvc.perform(delete("/api/libros/" + libro.getId())
                        .header("Authorization", "Bearer " + tokenDe(user)))
                .andExpect(status().isForbidden());
    }
}
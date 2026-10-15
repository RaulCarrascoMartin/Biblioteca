package com.example.demo.prestamo.web;

import com.example.demo.BaseIntegrationTest;
import com.example.demo.libro.domain.Libro;
import com.example.demo.usuario.domain.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PrestamoControllerTest extends BaseIntegrationTest {

    private Usuario admin;
    private Usuario ana;
    private Usuario bob;
    private Libro libroA;
    private Libro libroB;

    @BeforeEach
    void setUp() {
        limpiarBD();
        admin = crearUsuario("Admin", "padmin@test.es", "ADMIN");
        ana = crearUsuario("Ana", "pana@test.es", "USER");
        bob = crearUsuario("Bob", "pbob@test.es", "USER");
        libroA = crearLibro("Libro A");
        libroB = crearLibro("Libro B");
    }

    private long crearPrestamoComo(Usuario u, Libro l) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/prestamos")
                        .header("Authorization", "Bearer " + tokenDe(u))
                        .param("libroId", String.valueOf(l.getId())))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(res.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    @Test
    void crear_comoUser_CreaPrestamoYMarcaNoDisponible() throws Exception {
        mockMvc.perform(post("/api/prestamos")
                        .header("Authorization", "Bearer " + tokenDe(ana))
                        .param("libroId", String.valueOf(libroA.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libro.titulo").value("Libro A"))
                .andExpect(jsonPath("$.usuarioNombre").value("Ana"))
                .andExpect(jsonPath("$.fechaPrestamo").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.fechaDevolucion").isEmpty());

        assertFalse(libroRepository.findById(libroA.getId()).orElseThrow().isDisponible());
    }

    @Test
    void crear_libroNoDisponible_409() throws Exception {
        crearPrestamoComo(ana, libroA);
        mockMvc.perform(post("/api/prestamos")
                        .header("Authorization", "Bearer " + tokenDe(bob))
                        .param("libroId", String.valueOf(libroA.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    void crear_libroInexistente_404() throws Exception {
        mockMvc.perform(post("/api/prestamos")
                        .header("Authorization", "Bearer " + tokenDe(ana))
                        .param("libroId", "9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarTodos_comoAdmin_200Y_comoUser_403() throws Exception {
        crearPrestamoComo(ana, libroA);

        mockMvc.perform(get("/api/prestamos").header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/prestamos").header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarMios_soloMuestraLosPropios() throws Exception {
        crearPrestamoComo(ana, libroA);
        crearPrestamoComo(bob, libroB);

        mockMvc.perform(get("/api/prestamos/mios").header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].libro.titulo").value("Libro A"));

        mockMvc.perform(get("/api/prestamos/mios").header("Authorization", "Bearer " + tokenDe(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libro.titulo").value("Libro B"));
    }

    @Test
    void listarMios_sinPrestamos_vacio() throws Exception {
        mockMvc.perform(get("/api/prestamos/mios").header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void obtenerPorId_propietario_200_ajeno_403_admin_200() throws Exception {
        long prestamo = crearPrestamoComo(ana, libroA);

        mockMvc.perform(get("/api/prestamos/" + prestamo).header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(prestamo));

        mockMvc.perform(get("/api/prestamos/" + prestamo).header("Authorization", "Bearer " + tokenDe(bob)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/prestamos/" + prestamo).header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerPorId_inexistente_404() throws Exception {
        mockMvc.perform(get("/api/prestamos/9999").header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void devolver_propietario_200YVuelveDisponible() throws Exception {
        long prestamo = crearPrestamoComo(ana, libroA);

        mockMvc.perform(put("/api/prestamos/" + prestamo + "/devolver")
                        .header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaDevolucion").value(LocalDate.now().toString()));

        assertTrue(libroRepository.findById(libroA.getId()).orElseThrow().isDisponible());
    }

    @Test
    void devolver_usuarioAjeno_403() throws Exception {
        long prestamo = crearPrestamoComo(ana, libroA);
        mockMvc.perform(put("/api/prestamos/" + prestamo + "/devolver")
                        .header("Authorization", "Bearer " + tokenDe(bob)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devolver_inexistente_404() throws Exception {
        mockMvc.perform(put("/api/prestamos/9999/devolver")
                        .header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void devolver_dosVeces_409YNoReabreElLibroPrestadoAOtro() throws Exception {
        long prestamo = crearPrestamoComo(ana, libroA);
        mockMvc.perform(put("/api/prestamos/" + prestamo + "/devolver")
                        .header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isOk());
        crearPrestamoComo(bob, libroA);

        mockMvc.perform(put("/api/prestamos/" + prestamo + "/devolver")
                        .header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isConflict());

        assertTrue(!libroRepository.findById(libroA.getId()).orElseThrow().isDisponible());
    }
}

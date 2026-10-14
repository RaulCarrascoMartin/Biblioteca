package com.example.demo.container;

import com.example.demo.BaseIntegrationTest;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.persistance.repository.JpaUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerTest extends BaseIntegrationTest {

    private Usuario admin;
    private Usuario ana;
    private Usuario bob;

    @BeforeEach
    void setUp() {
        limpiarBD();
        admin = crearUsuario("Admin", "uadmin@test.es", "ADMIN");
        ana = crearUsuario("Ana", "uana@test.es", "USER");
        bob = crearUsuario("Bob", "ubob@test.es", "USER");
    }

    @Test
    void listar_comoAdmin_DevuelveTodos() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void listar_comoUser_Devuelve403() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPorId_propietario_200() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("uana@test.es"));
    }

    @Test
    void obtenerPorId_usuarioAjeno_403() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + bob.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPorId_comoAdmin_deOtroUser_200() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + bob.getId())
                        .header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ubob@test.es"));
    }

    @Test
    void obtenerMe_200() throws Exception {
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void obtenerMe_sinToken_401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actualizar_propio_CambiaNombreYEmail() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Ana María", "email", "ana2@test.es"));

        mockMvc.perform(put("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana María"))
                .andExpect(jsonPath("$.email").value("ana2@test.es"));

        assertTrue(usuarioRepository.findByEmail("ana2@test.es").isPresent());
    }

    @Test
    void actualizar_propio_CambiaPasswordYConservaElRol() throws Exception {
        String antigua = usuarioRepository.findById(ana.getId()).orElseThrow().getPassword();

        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Ana", "email", "uana@test.es", "password", "Nueva4567"));

        mockMvc.perform(put("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        String nueva = usuarioRepository.findById(ana.getId()).orElseThrow().getPassword();
        assertFalse(antigua.equals(nueva));
        assertTrue(nueva.startsWith("$2"));
    }

    @Test
    void actualizar_sinPassword_ConservalaExistente() throws Exception {
        String antigua = usuarioRepository.findById(ana.getId()).orElseThrow().getPassword();

        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Ana", "email", "uana@test.es"));

        mockMvc.perform(put("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        assertTrue(antigua.equals(
                usuarioRepository.findById(ana.getId()).orElseThrow().getPassword()));
    }

    @Test
    void actualizar_passwordCorta_400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Ana", "email", "uana@test.es", "password", "123"));

        mockMvc.perform(put("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        org.hamcrest.Matchers.containsString("8")));
    }

    @Test
    void actualizar_emailInvalido_400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Ana", "email", "no-es-correo"));

        mockMvc.perform(put("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_usuarioAjeno_403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Bob", "email", "ubob@test.es"));

        mockMvc.perform(put("/api/usuarios/" + bob.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizar_comoAdmin_deOtroUser_200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Bobby", "email", "ubob@test.es"));

        mockMvc.perform(put("/api/usuarios/" + bob.getId())
                        .header("Authorization", "Bearer " + tokenDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Bobby"));
    }

    @Test
    void eliminar_comoAdmin_204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isNoContent());

        assertTrue(usuarioRepository.findById(ana.getId()).isEmpty());
    }

    @Test
    void eliminar_comoUser_403() throws Exception {
        mockMvc.perform(delete("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isForbidden());
    }

    @Test
    void revocarSesiones_invalidaLosTokensAnterioresDelUsuario() throws Exception {
        String viejo = tokenDe(ana);
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + viejo))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/usuarios/" + ana.getId() + "/revocar-sesiones")
                        .header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + viejo))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + tokenDe(usuarioRepository.findById(ana.getId()).orElseThrow())))
                .andExpect(status().isOk());
    }

    @Test
    void revocarSesiones_deOtroUsuario_403() throws Exception {
        Usuario bob = crearUsuario("Bob", "revoca-bob@test.es", "USER");
        mockMvc.perform(post("/api/usuarios/" + bob.getId() + "/revocar-sesiones")
                        .header("Authorization", "Bearer " + tokenDe(ana)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cambiarPassword_invalidaTokensAnteriores() throws Exception {
        String viejo = tokenDe(ana);
        mockMvc.perform(put("/api/usuarios/" + ana.getId())
                        .header("Authorization", "Bearer " + viejo)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"email\":\"" + ana.getEmail() + "\",\"password\":\"NuevaClave123\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + viejo))
                .andExpect(status().isUnauthorized());
    }
}

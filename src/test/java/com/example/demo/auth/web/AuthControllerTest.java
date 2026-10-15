package com.example.demo.auth.web;

import com.example.demo.BaseIntegrationTest;
import com.example.demo.usuario.domain.Usuario;
import com.example.demo.usuario.persistence.JpaUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        limpiarBD();
        limpiarLimites();
    }

    @Test
    void register_CreaUsuarioConRolUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nombre", "Ana", "email", "ana@test.es", "password", "Clave1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@test.es"))
                .andExpect(jsonPath("$.rol").value("USER"));

        Usuario u = usuarioRepository.findByEmail("ana@test.es").orElseThrow();
        assertTrue(u.getPassword().startsWith("$2"));
    }

    @Test
    void register_IgnoraRolAdminDelCuerpo() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nombre", "X", "email", "x@test.es", "password", "Pass12345", "rol", "ADMIN"))))
                .andExpect(status().isOk());

        assertEquals("USER", usuarioRepository.findByEmail("x@test.es").orElseThrow().getRol());
    }

    @Test
    void register_EmailDuplicadoDevuelve200Vacio() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("nombre", "Ana", "email", "dup@test.es", "password", "Clave1234"));
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertEquals(1, usuarioRepository.findByEmail("dup@test.es").stream().count());
    }

    @Test
    void register_ContrasenaCorta_Devuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nombre", "A", "email", "short@test.es", "password", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("8")));
    }

    @Test
    void register_EmailInvalido_Devuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nombre", "A", "email", "no-es-correo", "password", "Clave1234"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_CredencialesValidas_DevuelveToken() throws Exception {
        Usuario u = crearUsuario("Lector", "lex@test.es", "USER");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "lex@test.es", "password", "Pass1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_PasswordIncorrecta_Devuelve401() throws Exception {
        crearUsuario("Lector", "lex2@test.es", "USER");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "lex2@test.es", "password", "mala"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @Test
    void login_EmailInexistente_Devuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "fantasma@test.es", "password", "Pass1234"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @Test
    void rateLimit_registerDevuelve429AlCuartoIntento() throws Exception {
        for (int i = 0; i < 3; i++) {
            String body = objectMapper.writeValueAsString(
                    Map.of("nombre", "U" + i, "email", "r" + i + "@test.es", "password", "Pass12345"));
            mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk());
        }

        String body = objectMapper.writeValueAsString(
                Map.of("nombre", "X", "email", "rX@test.es", "password", "Pass12345"));
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void rateLimit_loginDevuelve429AlSextoIntento() throws Exception {
        Usuario u = crearUsuario("Lector", "lex3@test.es", "USER");
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("email", "lex3@test.es", "password", "mala"))))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "lex3@test.es", "password", "mala"))))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void login_sinCamposDevuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_emailSeNormalizaAMinusculas() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nombre", "Ana", "email", "Ana@Test.es", "password", "Clave1234"))))
                .andExpect(status().isOk());
        usuarioRepository.findByEmail("ana@test.es").orElseThrow();
    }

    @Test
    void logout_conCookieRevocaElToken() throws Exception {
        Usuario u = crearUsuario("Ana", "cookie@test.es", "USER");
        String token = tokenDe(u);
        var cookie = new jakarta.servlet.http.Cookie("jwt_token", token);

        mockMvc.perform(get("/api/usuarios/me").cookie(cookie)).andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/logout").cookie(cookie)).andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios/me").cookie(cookie)).andExpect(status().isUnauthorized());
    }
}

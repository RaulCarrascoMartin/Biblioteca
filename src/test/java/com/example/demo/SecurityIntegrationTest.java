package com.example.demo;

import com.example.demo.users.UsersModule;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Seguridad extremo a extremo: rutas públicas, 401, par de tokens (login/refresh/logout), rate limit y hash en BD. */
class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsersModule usersModule;

    @BeforeEach
    void setUp() {
        cleanDb();
        resetLimits();
    }

    private String json(Map<String, String> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void mainPageIsPublic() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void indexIsPublic() throws Exception {
        mockMvc.perform(get("/index.html")).andExpect(status().isOk());
    }

    @Test
    void cssIsPublic() throws Exception {
        mockMvc.perform(get("/css/base.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }

    @Test
    void javascriptIsPublic() throws Exception {
        mockMvc.perform(get("/js/main.js")).andExpect(status().isOk());
    }

    @Test
    void apiWithoutTokenIs401Json() throws Exception {
        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autenticado"));
    }

    @Test
    void protectedEndpointWithoutTokenIs401Json() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autenticado"));
    }

    @Test
    void invalidTokenIs401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer basura"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void registerIsPublicAndStoresABcryptHash() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Ana", "email", "ana@test.es", "password", "Clave1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@test.es"))
                .andExpect(jsonPath("$.rol").value("USER"));

        String stored = jdbc.queryForObject("SELECT password FROM usuarios WHERE email = ?", String.class, "ana@test.es");
        assertTrue(stored.startsWith("$2"));
    }

    @Test
    void registerNormalizesEmailToLowerCase() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Ana", "email", "Ana@Test.es", "password", "Clave1234"))))
                .andExpect(status().isOk());

        assertTrue(usersModule.getByEmail("ana@test.es").isPresent());
    }

    @Test
    void registerIgnoresRoleFromBody() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "X", "email", "x@test.es", "password", "Pass12345", "rol", "ADMIN"))))
                .andExpect(status().isOk());

        assertEquals("USER", usersModule.getByEmail("x@test.es").orElseThrow().role());
    }

    private JsonNode login(String email) throws Exception {
        var result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Pass1234"))))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private org.springframework.test.web.servlet.ResultActions refresh(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refreshToken))));
    }

    @Test
    void loginReturnsATokenPairAndTheAccessTokenOpensProtectedEndpoints() throws Exception {
        createUser("Lector", "lex@test.es", "USER");

        JsonNode pair = login("lex@test.es");

        assertTrue(pair.get("refreshToken").asText().length() >= 32);
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + pair.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lex@test.es"));
    }

    @Test
    void theRefreshTokenIsStoredOnlyAsAHash() throws Exception {
        createUser("Lector", "hash@test.es", "USER");

        String refresh = login("hash@test.es").get("refreshToken").asText();

        String stored = jdbc.queryForObject("SELECT token_hash FROM refresh_tokens", String.class);
        assertNotEquals(refresh, stored);
        assertEquals(64, stored.length());
    }

    @Test
    void theOldJwtCookieNoLongerAuthenticates() throws Exception {
        createUser("Ana", "cookie@test.es", "USER");

        mockMvc.perform(get("/api/usuarios/me").cookie(new Cookie("jwt_token", tokenFor("cookie@test.es"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        createUser("Lector", "lex2@test.es", "USER");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "lex2@test.es", "password", "mala"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @Test
    void refreshRotatesTheTokenAndTheOldOneIsRejected() throws Exception {
        createUser("Ana", "rot@test.es", "USER");
        JsonNode first = login("rot@test.es");

        var result = refresh(first.get("refreshToken").asText()).andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty()).andReturn();
        JsonNode second = objectMapper.readTree(result.getResponse().getContentAsString());

        assertNotEquals(first.get("refreshToken").asText(), second.get("refreshToken").asText());
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + second.get("accessToken").asText()))
                .andExpect(status().isOk());
    }

    @Test
    void reusingARotatedRefreshRevokesTheWholeSession() throws Exception {
        createUser("Ana", "reuse@test.es", "USER");
        String old = login("reuse@test.es").get("refreshToken").asText();
        var rotated = refresh(old).andExpect(status().isOk()).andReturn();
        String current = objectMapper.readTree(rotated.getResponse().getContentAsString()).get("refreshToken").asText();

        refresh(old).andExpect(status().isUnauthorized());
        refresh(current).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredRefreshIs401() throws Exception {
        createUser("Ana", "exp@test.es", "USER");
        String refresh = login("exp@test.es").get("refreshToken").asText();
        jdbc.update("UPDATE refresh_tokens SET expires_at = TIMESTAMP '2000-01-01 00:00:00'");

        refresh(refresh).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownRefreshIs401AndMissingRefreshIs400() throws Exception {
        refresh("desconocido").andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutRevokesTheRefreshToken() throws Exception {
        createUser("Ana", "bearer@test.es", "USER");
        JsonNode pair = login("bearer@test.es");

        mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + pair.get("accessToken").asText())
                        .content(json(Map.of("refreshToken", pair.get("refreshToken").asText()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada"));

        refresh(pair.get("refreshToken").asText()).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshAfterRevokingSessionsIs401() throws Exception {
        long id = createUser("Ana", "ver@test.es", "USER");
        String refresh = login("ver@test.es").get("refreshToken").asText();
        jdbc.update("UPDATE usuarios SET token_version = token_version + 1 WHERE id = ?", id);

        refresh(refresh).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshIsPublicButLogoutNeedsTheAccessToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", "x"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerRateLimitReturns429OnTheFourthAttempt() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("nombre", "U" + i, "email", "r" + i + "@test.es", "password", "Pass12345"))))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "X", "email", "rX@test.es", "password", "Pass12345"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void loginRateLimitReturns429OnTheSixthAttempt() throws Exception {
        createUser("Lector", "lex3@test.es", "USER");
        String body = json(Map.of("email", "lex3@test.es", "password", "mala"));
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests());
    }
}

package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de acceso (plan 1.4) con seguridad real: por cada endpoint protegido, 401 sin token y 403 con
 * un usuario sin permiso (no ADMIN, o ni ADMIN ni dueño). La política vive en los casos de uso: este
 * test es la red que detecta un caso de uso que olvide comprobarla.
 */
class EndpointAuthorizationIntegrationTest extends BaseIntegrationTest {

    private static final String BOOK_BODY = "{\"titulo\":\"T\",\"autor\":\"A\",\"isbn\":\"9780000009999\"}";
    private static final String USER_BODY = "{\"nombre\":\"Nuevo\",\"email\":\"nuevo@test.es\"}";

    private long aliceId;
    private long bookId;
    private long loanId;

    @BeforeEach
    void setUp() {
        cleanDb();
        resetLimits();
        createUser("Admin", "admin@test.es", "ADMIN");
        aliceId = createUser("Alice", "alice@test.es", "USER");
        createUser("Bob", "bob@test.es", "USER");
        bookId = createBook("Libro");
        long loanedBook = createBook("Prestado");
        loanId = createLoan(aliceId, loanedBook);
    }

    private MockHttpServletRequestBuilder build(HttpMethod method, String path, String body, String auth) {
        String resolved = path.replace("{user}", String.valueOf(aliceId))
                .replace("{book}", String.valueOf(bookId))
                .replace("{loan}", String.valueOf(loanId));
        MockHttpServletRequestBuilder builder = request(method, resolved);
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        if (auth != null) {
            builder.header("Authorization", auth);
        }
        return builder;
    }

    /** Todos los endpoints protegidos (excepto los públicos de /api/auth). */
    static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                arguments(HttpMethod.GET, "/api/libros", null),
                arguments(HttpMethod.GET, "/api/libros/{book}", null),
                arguments(HttpMethod.POST, "/api/libros", BOOK_BODY),
                arguments(HttpMethod.PUT, "/api/libros/{book}", BOOK_BODY),
                arguments(HttpMethod.DELETE, "/api/libros/{book}", null),
                arguments(HttpMethod.GET, "/api/usuarios", null),
                arguments(HttpMethod.GET, "/api/usuarios/me", null),
                arguments(HttpMethod.GET, "/api/usuarios/{user}", null),
                arguments(HttpMethod.PUT, "/api/usuarios/{user}", USER_BODY),
                arguments(HttpMethod.POST, "/api/usuarios/{user}/revocar-sesiones", null),
                arguments(HttpMethod.DELETE, "/api/usuarios/{user}", null),
                arguments(HttpMethod.GET, "/api/prestamos", null),
                arguments(HttpMethod.GET, "/api/prestamos/mios", null),
                arguments(HttpMethod.GET, "/api/prestamos/{loan}", null),
                arguments(HttpMethod.POST, "/api/prestamos?libroId={book}", null),
                arguments(HttpMethod.PUT, "/api/prestamos/{loan}/devolver", null)
        );
    }

    /** Solo ADMIN: un USER recibe 403. */
    static Stream<Arguments> adminOnly() {
        return Stream.of(
                arguments(HttpMethod.POST, "/api/libros", BOOK_BODY),
                arguments(HttpMethod.PUT, "/api/libros/{book}", BOOK_BODY),
                arguments(HttpMethod.DELETE, "/api/libros/{book}", null),
                arguments(HttpMethod.GET, "/api/usuarios", null),
                arguments(HttpMethod.DELETE, "/api/usuarios/{user}", null),
                arguments(HttpMethod.GET, "/api/prestamos", null)
        );
    }

    /** ADMIN o dueño: otro usuario (Bob) recibe 403 sobre recursos de Alice. */
    static Stream<Arguments> ownerOrAdmin() {
        return Stream.of(
                arguments(HttpMethod.GET, "/api/usuarios/{user}", null),
                arguments(HttpMethod.PUT, "/api/usuarios/{user}", USER_BODY),
                arguments(HttpMethod.POST, "/api/usuarios/{user}/revocar-sesiones", null),
                arguments(HttpMethod.GET, "/api/prestamos/{loan}", null),
                arguments(HttpMethod.PUT, "/api/prestamos/{loan}/devolver", null)
        );
    }

    @DisplayName("sin token: 401")
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("protectedEndpoints")
    void withoutToken_returns401(HttpMethod method, String path, String body) throws Exception {
        mockMvc.perform(build(method, path, body, null))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autenticado"));
    }

    @DisplayName("USER en endpoint de ADMIN: 403")
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("adminOnly")
    void regularUserOnAdminEndpoint_returns403(HttpMethod method, String path, String body) throws Exception {
        mockMvc.perform(build(method, path, body, bearer("bob@test.es")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @DisplayName("otro usuario sobre recurso ajeno: 403")
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("ownerOrAdmin")
    void otherUserOnSomeoneElsesResource_returns403(HttpMethod method, String path, String body) throws Exception {
        mockMvc.perform(build(method, path, body, bearer("bob@test.es")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @DisplayName("el dueño sí accede a lo suyo")
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("ownerReads")
    void ownerCanAccessOwnResources(HttpMethod method, String path) throws Exception {
        mockMvc.perform(build(method, path, null, bearer("alice@test.es")))
                .andExpect(status().isOk());
    }

    static Stream<Arguments> ownerReads() {
        return Stream.of(
                arguments(HttpMethod.GET, "/api/usuarios/me"),
                arguments(HttpMethod.GET, "/api/usuarios/{user}"),
                arguments(HttpMethod.GET, "/api/prestamos/{loan}"),
                arguments(HttpMethod.GET, "/api/prestamos/mios"),
                arguments(HttpMethod.GET, "/api/libros")
        );
    }

    @DisplayName("ADMIN accede a todo lo restringido")
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("adminReads")
    void adminCanAccessRestrictedResources(HttpMethod method, String path) throws Exception {
        mockMvc.perform(build(method, path, null, bearer("admin@test.es")))
                .andExpect(status().isOk());
    }

    static Stream<Arguments> adminReads() {
        return Stream.of(
                arguments(HttpMethod.GET, "/api/usuarios"),
                arguments(HttpMethod.GET, "/api/usuarios/{user}"),
                arguments(HttpMethod.GET, "/api/prestamos"),
                arguments(HttpMethod.GET, "/api/prestamos/{loan}")
        );
    }
}

package com.example.demo;

import com.example.demo.config.RateLimitFilter;
import com.example.demo.shared.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * Base de los tests de integración (contexto completo con seguridad real). Solo usa API pública de
 * los módulos y {@code shared}; los datos de partida se siembran por JDBC sobre el esquema
 * histórico (D16), de modo que no se importan clases {@code internal}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected TokenProvider tokenProvider;

    @Autowired
    protected RateLimitFilter rateLimitFilter;

    private int seq = 0;

    protected void cleanDb() {
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM prestamos");
        jdbc.update("DELETE FROM libros");
        jdbc.update("DELETE FROM usuarios");
        seq = 0;
    }

    protected void resetLimits() {
        rateLimitFilter.reset();
    }

    protected long createUser(String name, String email, String role) {
        return insert("INSERT INTO usuarios (nombre, email, password, rol, token_version) VALUES (?, ?, ?, ?, 0)",
                name, email, passwordEncoder.encode("Pass1234"), role);
    }

    protected long createBook(String title) {
        return insert("INSERT INTO libros (titulo, autor, isbn, disponible, genero) VALUES (?, ?, ?, TRUE, 'CLASICA')",
                title, "Autor de prueba", String.format("978000000000%03d", seq++));
    }

    protected long createLoan(long userId, long bookId) {
        jdbc.update("UPDATE libros SET disponible = FALSE WHERE id = ?", bookId);
        return insert("INSERT INTO prestamos (libro_id, usuario_id, fecha_prestamo) VALUES (?, ?, CURRENT_DATE)",
                bookId, userId);
    }

    protected String tokenFor(String email) {
        var user = jdbc.queryForMap("SELECT id, rol FROM usuarios WHERE email = ?", email);
        return tokenProvider.generate(((Number) user.get("id")).longValue(), List.of("ROLE_" + user.get("rol")));
    }

    protected String bearer(String email) {
        return "Bearer " + tokenFor(email);
    }

    private long insert(String sql, Object... args) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keys);
        return ((Number) keys.getKeys().get("id")).longValue();
    }
}

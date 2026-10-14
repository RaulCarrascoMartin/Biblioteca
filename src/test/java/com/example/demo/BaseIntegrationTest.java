package com.example.demo;

import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.security.JwtService;
import com.example.demo.persistance.repository.JpaLibroRepository;
import com.example.demo.persistance.repository.JpaPrestamoRepository;
import com.example.demo.persistance.repository.JpaUsuarioRepository;
import com.example.demo.security.RateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected JpaUsuarioRepository usuarioRepository;

    @Autowired
    protected JpaLibroRepository libroRepository;

    @Autowired
    protected JpaPrestamoRepository prestamoRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected RateLimitFilter rateLimitFilter;

    private int seq = 0;

    protected void limpiarBD() {
        prestamoRepository.deleteAll();
        libroRepository.deleteAll();
        usuarioRepository.deleteAll();
        seq = 0;
    }

    protected void limpiarLimites() {
        rateLimitFilter.reset();
    }

    protected Usuario crearUsuario(String nombre, String email, String rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setRol(rol);
        u.setPassword(passwordEncoder.encode("Pass1234"));
        return usuarioRepository.save(u);
    }

    protected Libro crearLibro(String titulo) {
        Libro l = new Libro();
        l.setTitulo(titulo);
        l.setAutor("Autor de prueba");
        l.setIsbn(String.format("978000000000%03d", seq++));
        return libroRepository.save(l);
    }

    protected String tokenDe(Usuario u) {
        return jwtService.generarToken(u.getEmail(), u.getTokenVersion());
    }
}
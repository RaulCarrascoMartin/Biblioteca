package com.example.demo.domain.service;

import com.example.demo.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "clave-de-prueba-segura-de-mas-de-32-bytes-abc";
    private static final String EMAIL = "lector@test.es";

    private JwtService servicio;

    @BeforeEach
    void setUp() {
        servicio = new JwtService(SECRET, 1_800_000L, "biblioteca-api", "biblioteca-web");
    }

    @Test
    void generaTokenYRecuperaElEmail() {
        String token = servicio.generarToken(EMAIL);
        assertEquals(EMAIL, servicio.extraerEmail(token));
    }

    @Test
    void tokenGeneradoEsValidoParaSuEmail() {
        String token = servicio.generarToken(EMAIL);
        assertTrue(servicio.esTokenValido(token, EMAIL));
    }

    @Test
    void tokenNoEsValidoParaOtroEmail() {
        String token = servicio.generarToken(EMAIL);
        assertFalse(servicio.esTokenValido(token, "otro@test.es"));
    }

    @Test
    void tokenManipuladoSeRechaza() {
        String token = servicio.generarToken(EMAIL);
        String mutado = token.substring(0, token.length() - 2)
                + (token.endsWith("a") ? "bb" : "aa");
        assertThrows(JwtException.class, () -> servicio.extraerEmail(mutado));
    }

    @Test
    void tokenCaducadoNoEsValido() throws InterruptedException {
        JwtService caducado = new JwtService(SECRET, 1L, "biblioteca-api", "biblioteca-web");
        String token = caducado.generarToken(EMAIL);
        Thread.sleep(10);
        assertFalse(caducado.esTokenValido(token, EMAIL));
    }

    @Test
    void tokenConOtroEmisorNoEsValido() {
        JwtService otroEmisor = new JwtService(SECRET, 1_800_000L, "otro-emisor", "biblioteca-web");
        String token = otroEmisor.generarToken(EMAIL);
        assertFalse(servicio.esTokenValido(token, EMAIL));
    }

    @Test
    void tokenConOtraAudienciaNoEsValido() {
        JwtService otraAudiencia = new JwtService(SECRET, 1_800_000L, "biblioteca-api", "otra-audiencia");
        String token = otraAudiencia.generarToken(EMAIL);
        assertFalse(servicio.esTokenValido(token, EMAIL));
    }

    @Test
    void secretoDemasiadoCortoLanzaError() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("corta", 1_800_000L, "biblioteca-api", "biblioteca-web"));
    }

    @Test
    void secretoNuloLanzaError() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService(null, 1_800_000L, "biblioteca-api", "biblioteca-web"));
    }
}
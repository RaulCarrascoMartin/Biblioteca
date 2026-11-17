package com.example.demo.auth.internal.controller;

import com.example.demo.auth.internal.controller.dto.LoginDto;
import com.example.demo.auth.internal.controller.dto.RegisterDto;
import com.example.demo.shared.Requester;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrato HTTP de autenticación. Códigos: 200 éxito (register con email repetido también, sin
 * cuerpo); 400 formato; 401 credenciales incorrectas (login) o sin sesión (logout);
 * 429 demasiados intentos (lo aplica {@code config}).
 */
@RequestMapping("/api/auth")
public interface AuthHttpApi {

    /** Público. Siempre crea el usuario con rol USER. */
    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto);

    /** Público. Devuelve el token en el cuerpo y en la cookie {@code jwt_token}. */
    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginDto dto, HttpServletRequest request);

    /** Autenticado. Revoca el token de la cabecera Authorization y el de la cookie. */
    @PostMapping("/logout")
    ResponseEntity<?> logout(Requester requester,
                             @RequestHeader(value = "Authorization", required = false) String authorization,
                             @CookieValue(value = "jwt_token", required = false) String cookieToken,
                             HttpServletRequest request);
}

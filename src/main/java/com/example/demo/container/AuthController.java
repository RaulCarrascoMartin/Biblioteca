package com.example.demo.container;

import com.example.demo.domain.dto.LoginDTO;
import com.example.demo.domain.dto.UsuarioDTO;
import com.example.demo.domain.dto.UsuarioResponseDTO;
import com.example.demo.domain.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioDTO dto) {
        return authService.registrar(dto)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(new UsuarioResponseDTO(u)))
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO credenciales, HttpServletRequest request) {
        String token = authService.login(credenciales.getEmail(), credenciales.getPassword());

        ResponseCookie cookie = ResponseCookie.from("jwt_token", token)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.revocarToken(authHeader.substring(7));
        }
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("jwt_token".equals(c.getName())) {
                    authService.revocarToken(c.getValue());
                }
            }
        }

        ResponseCookie clearCookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(Map.of("message", "Sesión cerrada"));
    }

}

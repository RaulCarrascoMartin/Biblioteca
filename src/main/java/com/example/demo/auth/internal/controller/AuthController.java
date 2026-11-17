package com.example.demo.auth.internal.controller;

import com.example.demo.auth.internal.controller.dto.LoginDto;
import com.example.demo.auth.internal.controller.dto.RegisterDto;
import com.example.demo.auth.internal.usecase.AuthUseCase;
import com.example.demo.shared.Requester;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Traduce HTTP a llamadas al caso de uso; gestiona la cookie de sesión. */
@RestController
public class AuthController implements AuthHttpApi {

    private static final String COOKIE = "jwt_token";
    private static final String BEARER = "Bearer ";

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @Override
    public ResponseEntity<?> register(RegisterDto dto) {
        return authUseCase.register(dto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    @Override
    public ResponseEntity<?> login(LoginDto dto, HttpServletRequest request) {
        String token = authUseCase.login(dto);

        ResponseCookie cookie = ResponseCookie.from(COOKIE, token)
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

    @Override
    public ResponseEntity<?> logout(Requester requester, String authorization, String cookieToken,
                                    HttpServletRequest request) {
        List<String> tokens = new ArrayList<>();
        if (authorization != null && authorization.startsWith(BEARER)) {
            tokens.add(authorization.substring(BEARER.length()));
        }
        if (cookieToken != null) {
            tokens.add(cookieToken);
        }
        authUseCase.logout(requester, tokens);

        ResponseCookie clearCookie = ResponseCookie.from(COOKIE, "")
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

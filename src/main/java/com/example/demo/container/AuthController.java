package com.example.demo.container;


import com.example.demo.domain.dto.LoginDTO;
import com.example.demo.domain.dto.UsuarioDTO;
import com.example.demo.domain.dto.UsuarioResponseDTO;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.domain.service.JwtService;
import com.example.demo.persistance.repository.UsuarioRepository;
import com.example.demo.security.TokenDenylistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenDenylistService tokenDenylistService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.ok().build();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol("USER");

        Usuario guardado = usuarioRepository.save(usuario);

        return ResponseEntity.ok(new UsuarioResponseDTO(guardado));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO credenciales, HttpServletRequest request){
        String email = credenciales.getEmail().trim().toLowerCase();
        String password = credenciales.getPassword();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        int version = usuarioRepository.findByEmail(email).map(Usuario::getTokenVersion).orElse(0);
        String token = jwtService.generarToken(email, version);

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
            tokenDenylistService.revoke(authHeader.substring(7));
        }
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("jwt_token".equals(c.getName())) {
                    tokenDenylistService.revoke(c.getValue());
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
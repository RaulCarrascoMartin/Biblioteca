package com.example.demo.auth.domain;

import com.example.demo.usuario.domain.UsuarioDTO;
import com.example.demo.usuario.domain.Usuario;
import com.example.demo.shared.PasswordHasher;
import com.example.demo.usuario.domain.UsuarioRepository;
import com.example.demo.usuario.domain.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final CredentialsAuthenticator credentialsAuthenticator;
    private final TokenService tokenService;
    private final TokenRevocation tokenRevocation;

    /** Devuelve vacío si el email ya estaba registrado (no se revela al cliente). */
    @Transactional
    public Optional<UsuarioResponseDTO> registrar(UsuarioDTO dto) {
        String email = dto.email().trim().toLowerCase();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return Optional.empty();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordHasher.encode(dto.password()));
        usuario.setRol("USER");

        return Optional.of(new UsuarioResponseDTO(usuarioRepository.save(usuario)));
    }

    public String login(String email, String password) {
        String emailNormalizado = email.trim().toLowerCase();
        credentialsAuthenticator.autenticar(emailNormalizado, password);

        int version = usuarioRepository.findByEmail(emailNormalizado).map(Usuario::getTokenVersion).orElse(0);
        return tokenService.generarToken(emailNormalizado, version);
    }

    public void revocarToken(String token) {
        tokenRevocation.revoke(token);
    }
}

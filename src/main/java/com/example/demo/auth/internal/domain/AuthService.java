package com.example.demo.auth.internal.domain;

import com.example.demo.usuario.UsuarioModule;
import com.example.demo.usuario.dto.UsuarioCredencialesDTO;
import com.example.demo.usuario.dto.UsuarioDTO;
import com.example.demo.usuario.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioModule usuarioModule;
    private final CredentialsAuthenticator credentialsAuthenticator;
    private final TokenService tokenService;
    private final TokenRevocation tokenRevocation;

    /** Devuelve vacío si el email ya estaba registrado (no se revela al cliente). */
    public Optional<UsuarioResponseDTO> registrar(UsuarioDTO dto) {
        return usuarioModule.registrar(dto);
    }

    public String login(String email, String password) {
        String emailNormalizado = email.trim().toLowerCase();
        credentialsAuthenticator.autenticar(emailNormalizado, password);

        int version = usuarioModule.obtenerCredenciales(emailNormalizado)
                .map(UsuarioCredencialesDTO::tokenVersion).orElse(0);
        return tokenService.generarToken(emailNormalizado, version);
    }

    public void revocarToken(String token) {
        tokenRevocation.revoke(token);
    }
}

package com.example.demo.usuario.domain;

import com.example.demo.shared.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final List<UsuarioEnUsoChecker> enUsoCheckers;
    private final PasswordHasher passwordHasher;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream().map(UsuarioResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDTO> obtenerPorId(Long id) {
        return usuarioRepository.findById(id).map(UsuarioResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDTO> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email).map(UsuarioResponseDTO::new);
    }

    /** Devuelve vacío si el usuario no existe. */
    @Transactional
    public Optional<UsuarioResponseDTO> actualizar(Long id, UsuarioUpdateDTO usuarioNuevo) {
        return usuarioRepository.findById(id).map(existente -> {
            String nuevoEmail = usuarioNuevo.email().trim().toLowerCase();
            if (!existente.getEmail().equalsIgnoreCase(nuevoEmail)
                    && usuarioRepository.findByEmail(nuevoEmail).isPresent()) {
                throw new EmailYaRegistradoException();
            }
            existente.setNombre(usuarioNuevo.nombre().trim());
            existente.setEmail(nuevoEmail);

            if (usuarioNuevo.password() != null && !usuarioNuevo.password().isBlank()) {
                existente.setPassword(passwordHasher.encode(usuarioNuevo.password()));
                existente.setTokenVersion(existente.getTokenVersion() + 1);
            }

            return new UsuarioResponseDTO(usuarioRepository.save(existente));
        });
    }

    @Transactional
    public boolean revocarSesiones(Long id) {
        return usuarioRepository.findById(id).map(u -> {
            u.setTokenVersion(u.getTokenVersion() + 1);
            usuarioRepository.save(u);
            return true;
        }).orElse(false);
    }

    /** @param emailSolicitante email de quien pide la eliminación; no puede eliminarse a sí mismo */
    @Transactional
    public void eliminar(Long id, String emailSolicitante) {
        boolean esElMismo = usuarioRepository.findByEmail(emailSolicitante)
                .map(u -> u.getId().equals(id))
                .orElse(false);
        if (esElMismo) {
            throw new AutoEliminacionException();
        }
        if (enUsoCheckers.stream().anyMatch(c -> c.estaEnUso(id))) {
            throw new UsuarioConPrestamosException();
        }
        usuarioRepository.deleteById(id);
    }

}

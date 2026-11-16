package com.example.demo.usuario.internal.domain;

import com.example.demo.shared.PasswordHasher;
import com.example.demo.usuario.UsuarioEnUsoChecker;
import com.example.demo.usuario.dto.UsuarioCredencialesDTO;
import com.example.demo.usuario.dto.UsuarioDTO;
import com.example.demo.usuario.dto.UsuarioResponseDTO;
import com.example.demo.usuario.internal.domain.exception.AutoEliminacionException;
import com.example.demo.usuario.internal.domain.exception.EmailYaRegistradoException;
import com.example.demo.usuario.internal.domain.exception.UsuarioConPrestamosException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final List<UsuarioEnUsoChecker> enUsoCheckers;
    private final PasswordHasher passwordHasher;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDTO> obtenerPorId(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDTO> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email).map(usuarioMapper::toResponse);
    }

    /** Varios usuarios de una vez (evita N consultas), indexados por id. Los ids inexistentes se omiten. */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, UsuarioResponseDTO> obtenerPorIds(Collection<Long> ids) {
        return usuarioRepository.findAllById(ids).stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toMap(UsuarioResponseDTO::id, Function.identity()));
    }

    /** Devuelve vacío si el email ya estaba registrado (no se revela al cliente). */
    @Override
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

        return Optional.of(usuarioMapper.toResponse(usuarioRepository.save(usuario)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioCredencialesDTO> obtenerCredenciales(String email) {
        return usuarioRepository.findByEmail(email).map(usuarioMapper::toCredenciales);
    }

    /** Devuelve vacío si el usuario no existe. */
    @Override
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

            return usuarioMapper.toResponse(usuarioRepository.save(existente));
        });
    }

    @Override
    @Transactional
    public boolean revocarSesiones(Long id) {
        return usuarioRepository.findById(id).map(u -> {
            u.setTokenVersion(u.getTokenVersion() + 1);
            usuarioRepository.save(u);
            return true;
        }).orElse(false);
    }

    /** @param emailSolicitante email de quien pide la eliminación; no puede eliminarse a sí mismo */
    @Override
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

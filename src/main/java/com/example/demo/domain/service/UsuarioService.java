package com.example.demo.domain.service;

import lombok.RequiredArgsConstructor;

import com.example.demo.domain.dto.UsuarioUpdateDTO;
import com.example.demo.domain.port.PasswordHasher;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.domain.port.PrestamoRepository;
import com.example.demo.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;

    private final PasswordHasher passwordHasher;

    public List<Usuario> obtenerTodos(){
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerPorId(Long id){
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> obtenerPorEmail(String email){
        return usuarioRepository.findByEmail(email);
    }

    public Usuario guardar(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> actualizar(Long id, UsuarioUpdateDTO usuarioNuevo) {
        return usuarioRepository.findById(id).map(existente -> {
            String nuevoEmail = usuarioNuevo.getEmail().trim().toLowerCase();
            if (!existente.getEmail().equalsIgnoreCase(nuevoEmail)) {
                if (usuarioRepository.findByEmail(nuevoEmail).isPresent()) {
                    throw new IllegalArgumentException("El email ya está registrado");
                }
            }
            existente.setNombre(usuarioNuevo.getNombre().trim());
            existente.setEmail(nuevoEmail);

            if (usuarioNuevo.getPassword() != null && !usuarioNuevo.getPassword().isBlank()) {
                existente.setPassword(passwordHasher.encode(usuarioNuevo.getPassword()));
                existente.setTokenVersion(existente.getTokenVersion() + 1);
            }

            return usuarioRepository.save(existente);
        });
    }

    public boolean revocarSesiones(Long id) {
        return usuarioRepository.findById(id).map(u -> {
            u.setTokenVersion(u.getTokenVersion() + 1);
            usuarioRepository.save(u);
            return true;
        }).orElse(false);
    }

    public void eliminar(Long id){
        if (prestamoRepository.existsByUsuarioId(id)) {
            throw new IllegalStateException("No se puede eliminar un usuario con préstamos registrados");
        }
        usuarioRepository.deleteById(id);
    }

}
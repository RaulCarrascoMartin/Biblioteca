package com.example.demo.usuario.internal.application;

import com.example.demo.usuario.UsuarioModule;
import com.example.demo.usuario.dto.UsuarioCredencialesDTO;
import com.example.demo.usuario.dto.UsuarioDTO;
import com.example.demo.usuario.dto.UsuarioResponseDTO;
import com.example.demo.usuario.internal.domain.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/** Implementación de la API pública: solo delega en el servicio. */
@Component
@RequiredArgsConstructor
public class UsuarioModuleImpl implements UsuarioModule {

    private final UsuarioService usuarioService;

    @Override
    public Optional<UsuarioResponseDTO> obtenerPorId(Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @Override
    public Optional<UsuarioResponseDTO> obtenerPorEmail(String email) {
        return usuarioService.obtenerPorEmail(email);
    }

    @Override
    public Map<Long, UsuarioResponseDTO> obtenerPorIds(Collection<Long> ids) {
        return usuarioService.obtenerPorIds(ids);
    }

    @Override
    public Optional<UsuarioResponseDTO> registrar(UsuarioDTO dto) {
        return usuarioService.registrar(dto);
    }

    @Override
    public Optional<UsuarioCredencialesDTO> obtenerCredenciales(String email) {
        return usuarioService.obtenerCredenciales(email);
    }
}

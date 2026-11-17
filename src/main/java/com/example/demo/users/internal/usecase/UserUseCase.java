package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Flujos de usuario sobre las cuentas. Aquí vive toda la política de acceso: listar y borrar es de
 * ADMIN; obtener, editar y revocar sesiones, de ADMIN o del dueño; {@code me}, de cualquier
 * autenticado. Las reglas de negocio las aplica {@link UserService}.
 */
@Component
public class UserUseCase {

    private final UserService userService;

    public UserUseCase(UserService userService) {
        this.userService = userService;
    }

    public List<UserDto> getAll(Requester requester) {
        requireAdmin(requester);
        return userService.getAll();
    }

    public UserDto me(Requester requester) {
        if (requester.id() == null) {
            throw new ForbiddenException();
        }
        return userService.getById(requester.id());
    }

    public UserDto getById(Requester requester, Long id) {
        requireAdminOrOwner(requester, id);
        return userService.getById(id);
    }

    public UserDto update(Requester requester, Long id, UserUpdateDto dto) {
        requireAdminOrOwner(requester, id);
        return userService.update(id, dto);
    }

    public void revokeSessions(Requester requester, Long id) {
        requireAdminOrOwner(requester, id);
        userService.revokeSessions(id);
    }

    public void delete(Requester requester, Long id) {
        requireAdmin(requester);
        userService.delete(id, requester.id());
    }

    private static void requireAdmin(Requester requester) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
    }

    private static void requireAdminOrOwner(Requester requester, Long id) {
        if (!requester.isAdmin() && !requester.isOwner(id)) {
            throw new ForbiddenException();
        }
    }
}

package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.dto.UserUpdateRequest;
import com.example.demo.users.internal.controller.mapper.UserWebMapper;
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
    private final UserWebMapper mapper;

    public UserUseCase(UserService userService, UserWebMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    public List<UserResponse> getAll(Requester requester) {
        requireAdmin(requester);
        return mapper.toResponses(userService.getAll());
    }

    public UserResponse me(Requester requester) {
        if (requester.id() == null) {
            throw new ForbiddenException();
        }
        return mapper.toResponse(userService.getById(requester.id()));
    }

    public UserResponse getById(Requester requester, Long id) {
        requireAdminOrOwner(requester, id);
        return mapper.toResponse(userService.getById(id));
    }

    public UserResponse update(Requester requester, Long id, UserUpdateRequest request) {
        requireAdminOrOwner(requester, id);
        return mapper.toResponse(userService.update(id, mapper.toDto(request)));
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

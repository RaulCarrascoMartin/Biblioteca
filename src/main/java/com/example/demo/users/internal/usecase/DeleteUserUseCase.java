package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.service.UserService;
import org.springframework.stereotype.Component;

/** Elimina un usuario. Política: solo ADMIN (el servicio impide borrarse a uno mismo). */
@Component
public class DeleteUserUseCase {

    private final UserService userService;

    public DeleteUserUseCase(UserService userService) {
        this.userService = userService;
    }

    public void execute(Requester requester, Long id) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
        userService.delete(id, requester.id());
    }
}

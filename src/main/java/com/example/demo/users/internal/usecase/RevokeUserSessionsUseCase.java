package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.service.UserService;
import org.springframework.stereotype.Component;

/** Revoca las sesiones de un usuario. Política: ADMIN o el propio usuario. */
@Component
public class RevokeUserSessionsUseCase {

    private final UserService userService;

    public RevokeUserSessionsUseCase(UserService userService) {
        this.userService = userService;
    }

    public void execute(Requester requester, Long id) {
        if (!requester.isAdmin() && !requester.isOwner(id)) {
            throw new ForbiddenException();
        }
        userService.revokeSessions(id);
    }
}

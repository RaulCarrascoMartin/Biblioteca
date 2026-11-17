package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.service.UserService;
import org.springframework.stereotype.Component;

/** Obtiene un usuario. Política: ADMIN o el propio usuario. */
@Component
public class GetUserUseCase {

    private final UserService userService;

    public GetUserUseCase(UserService userService) {
        this.userService = userService;
    }

    public UserDto execute(Requester requester, Long id) {
        if (!requester.isAdmin() && !requester.isOwner(id)) {
            throw new ForbiddenException();
        }
        return userService.getById(id);
    }
}

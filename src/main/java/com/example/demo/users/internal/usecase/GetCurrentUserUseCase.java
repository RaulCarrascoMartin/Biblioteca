package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.service.UserService;
import org.springframework.stereotype.Component;

/** Devuelve el usuario autenticado. Política: cualquier usuario con identidad. */
@Component
public class GetCurrentUserUseCase {

    private final UserService userService;

    public GetCurrentUserUseCase(UserService userService) {
        this.userService = userService;
    }

    public UserDto execute(Requester requester) {
        if (requester.id() == null) {
            throw new ForbiddenException();
        }
        return userService.getById(requester.id());
    }
}

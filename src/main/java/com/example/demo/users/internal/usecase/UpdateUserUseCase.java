package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.service.UserService;
import org.springframework.stereotype.Component;

/** Edita un usuario. Política: ADMIN o el propio usuario. */
@Component
public class UpdateUserUseCase {

    private final UserService userService;

    public UpdateUserUseCase(UserService userService) {
        this.userService = userService;
    }

    public UserDto execute(Requester requester, Long id, UserUpdateDto dto) {
        if (!requester.isAdmin() && !requester.isOwner(id)) {
            throw new ForbiddenException();
        }
        return userService.update(id, dto);
    }
}

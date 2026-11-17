package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.service.UserService;
import java.util.List;
import org.springframework.stereotype.Component;

/** Lista los usuarios. Política: solo ADMIN. */
@Component
public class GetAllUsersUseCase {

    private final UserService userService;

    public GetAllUsersUseCase(UserService userService) {
        this.userService = userService;
    }

    public List<UserDto> execute(Requester requester) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
        return userService.getAll();
    }
}

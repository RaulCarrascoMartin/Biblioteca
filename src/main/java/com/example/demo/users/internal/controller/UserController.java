package com.example.demo.users.internal.controller;

import com.example.demo.shared.Requester;
import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.mapper.UserWebMapper;
import com.example.demo.users.internal.controller.dto.UserUpdateRequest;
import com.example.demo.users.internal.usecase.DeleteUserUseCase;
import com.example.demo.users.internal.usecase.GetAllUsersUseCase;
import com.example.demo.users.internal.usecase.GetCurrentUserUseCase;
import com.example.demo.users.internal.usecase.GetUserUseCase;
import com.example.demo.users.internal.usecase.RevokeUserSessionsUseCase;
import com.example.demo.users.internal.usecase.UpdateUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Traduce HTTP a llamadas al caso de uso. La autorización la deciden los casos de uso. */
@RestController
public class UserController implements UserHttpApi {

    private final GetAllUsersUseCase getAllUsersUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final RevokeUserSessionsUseCase revokeUserSessionsUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserWebMapper mapper;

    public UserController(GetAllUsersUseCase getAllUsersUseCase,
                          GetCurrentUserUseCase getCurrentUserUseCase,
                          GetUserUseCase getUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          RevokeUserSessionsUseCase revokeUserSessionsUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          UserWebMapper mapper) {
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.revokeUserSessionsUseCase = revokeUserSessionsUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.mapper = mapper;
    }

    @Override
    public List<UserResponse> getAll(Requester requester) {
        return mapper.toResponses(getAllUsersUseCase.execute(requester));
    }

    @Override
    public UserResponse me(Requester requester) {
        return mapper.toResponse(getCurrentUserUseCase.execute(requester));
    }

    @Override
    public UserResponse getById(Requester requester, Long id) {
        return mapper.toResponse(getUserUseCase.execute(requester, id));
    }

    @Override
    public UserResponse update(Requester requester, Long id, UserUpdateRequest request) {
        return mapper.toResponse(updateUserUseCase.execute(requester, id, mapper.toDto(request)));
    }

    @Override
    public ResponseEntity<Void> revokeSessions(Requester requester, Long id) {
        revokeUserSessionsUseCase.execute(requester, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(Requester requester, Long id) {
        deleteUserUseCase.execute(requester, id);
        return ResponseEntity.noContent().build();
    }
}

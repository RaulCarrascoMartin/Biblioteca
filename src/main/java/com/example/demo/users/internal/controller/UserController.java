package com.example.demo.users.internal.controller;

import com.example.demo.shared.Requester;
import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.mapper.UserWebMapper;
import com.example.demo.users.internal.controller.dto.UserUpdateRequest;
import com.example.demo.users.internal.usecase.UserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Traduce HTTP a llamadas al caso de uso. La autorización la decide {@link UserUseCase}. */
@RestController
public class UserController implements UserHttpApi {

    private final UserUseCase userUseCase;
    private final UserWebMapper mapper;

    public UserController(UserUseCase userUseCase, UserWebMapper mapper) {
        this.userUseCase = userUseCase;
        this.mapper = mapper;
    }

    @Override
    public List<UserResponse> getAll(Requester requester) {
        return mapper.toResponses(userUseCase.getAll(requester));
    }

    @Override
    public UserResponse me(Requester requester) {
        return mapper.toResponse(userUseCase.me(requester));
    }

    @Override
    public UserResponse getById(Requester requester, Long id) {
        return mapper.toResponse(userUseCase.getById(requester, id));
    }

    @Override
    public UserResponse update(Requester requester, Long id, UserUpdateRequest request) {
        return mapper.toResponse(userUseCase.update(requester, id, mapper.toDto(request)));
    }

    @Override
    public ResponseEntity<Void> revokeSessions(Requester requester, Long id) {
        userUseCase.revokeSessions(requester, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(Requester requester, Long id) {
        userUseCase.delete(requester, id);
        return ResponseEntity.noContent().build();
    }
}

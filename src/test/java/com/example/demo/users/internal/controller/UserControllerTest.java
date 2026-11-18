package com.example.demo.users.internal.controller;

import com.example.demo.config.security.SecurityConfig;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.shared.TokenProvider;
import com.example.demo.users.internal.controller.mapper.UserWebMapperImpl;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.SelfDeletionException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.exception.UserWithLoansException;
import com.example.demo.users.internal.usecase.DeleteUserUseCase;
import com.example.demo.users.internal.usecase.GetAllUsersUseCase;
import com.example.demo.users.internal.usecase.GetCurrentUserUseCase;
import com.example.demo.users.internal.usecase.GetUserUseCase;
import com.example.demo.users.internal.usecase.RevokeUserSessionsUseCase;
import com.example.demo.users.internal.usecase.UpdateUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static com.example.demo.support.TestAuthentication.asUser;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba HTTP del controller con la {@code SecurityConfig} real: 401 sin identidad, 403 cuando el caso
 * de uso (simulado) deniega, y traducción de errores. La política de acceso se prueba en
 * los tests de los casos de uso.
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, UserWebMapperImpl.class})
class UserControllerTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final String UPDATE_BODY = "{\"name\":\"Ana\",\"email\":\"ana@x.es\",\"password\":\"Nueva4567\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAllUsersUseCase getAllUsersUseCase;

    @MockitoBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private RevokeUserSessionsUseCase revokeUserSessionsUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;
    // Colaborador del filtro JWT de la SecurityConfig real (la identidad se publica con TestAuthentication)
    @MockitoBean
    private TokenProvider tokenProvider;

    private static org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return asUser(1L, "ADMIN");
    }

    private static UserDto ana() {
        return new UserDto(2L, "Ana", "ana@x.es", "USER");
    }

    // ---- 401 / 403

    @Test
    void everyEndpointIs401WithoutIdentity() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/2")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/users/2").contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/users/2/revoke-sessions")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/users/2")).andExpect(status().isUnauthorized());

        verifyNoInteractions(getAllUsersUseCase, getCurrentUserUseCase, getUserUseCase, updateUserUseCase, revokeUserSessionsUseCase, deleteUserUseCase);
    }

    @Test
    void forbiddenFromUseCaseIs403() throws Exception {
        Requester bob = new Requester(3L, Set.of("USER"));
        when(getAllUsersUseCase.execute(bob)).thenThrow(new ForbiddenException());
        doThrow(new ForbiddenException()).when(deleteUserUseCase).execute(bob, 2L);

        mockMvc.perform(get("/api/users").with(asUser(3L, "USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/users/2").with(asUser(3L, "USER")))
                .andExpect(status().isForbidden());
    }

    // ---- éxito y contrato

    @Test
    void getAllReturnsUsersWithHistoricalFieldNamesAndNoPassword() throws Exception {
        when(getAllUsersUseCase.execute(ADMIN)).thenReturn(List.of(ana()));

        mockMvc.perform(get("/api/users").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        when(getCurrentUserUseCase.execute(ADMIN)).thenReturn(ana());

        mockMvc.perform(get("/api/users/me").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@x.es"));
    }

    @Test
    void getByIdReturnsUser() throws Exception {
        when(getUserUseCase.execute(ADMIN, 2L)).thenReturn(ana());

        mockMvc.perform(get("/api/users/2").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getByIdUnknownIs404ProblemDetail() throws Exception {
        when(getUserUseCase.execute(ADMIN, 9L)).thenThrow(new UserNotFoundException(9L));

        mockMvc.perform(get("/api/users/9").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(containsString("9")))
                .andExpect(jsonPath("$.error").value(containsString("9")));
    }

    @Test
    void updateMapsRequestToDtoAndReturnsUpdatedUser() throws Exception {
        when(updateUserUseCase.execute(ADMIN, 2L, new UserUpdateDto("Ana", "ana@x.es", "Nueva4567"))).thenReturn(ana());

        mockMvc.perform(put("/api/users/2").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"));
    }

    @Test
    void updateWithShortPasswordIs400ProblemDetailWithField() throws Exception {
        mockMvc.perform(put("/api/users/2").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@x.es\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]").value(containsString("password")))
                .andExpect(jsonPath("$.error").value(containsString("8")));
    }

    @Test
    void updateWithInvalidEmailIs400() throws Exception {
        mockMvc.perform(put("/api/users/2").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"no-es-correo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("email"))));
    }

    @Test
    void updateWithRepeatedEmailIs409() throws Exception {
        when(updateUserUseCase.execute(any(), any(), any())).thenThrow(new EmailAlreadyRegisteredException());

        mockMvc.perform(put("/api/users/2").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"bob@x.es\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value(containsString("registrado")));
    }

    @Test
    void revokeSessionsIs204() throws Exception {
        mockMvc.perform(post("/api/users/2/revoke-sessions").with(admin()))
                .andExpect(status().isNoContent());

        verify(revokeUserSessionsUseCase).execute(ADMIN, 2L);
    }

    @Test
    void deleteIs204() throws Exception {
        mockMvc.perform(delete("/api/users/2").with(admin()))
                .andExpect(status().isNoContent());

        verify(deleteUserUseCase).execute(ADMIN, 2L);
    }

    @Test
    void deleteUserWithLoansIs409() throws Exception {
        doThrow(new UserWithLoansException()).when(deleteUserUseCase).execute(ADMIN, 2L);

        mockMvc.perform(delete("/api/users/2").with(admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("préstamos")));
    }

    @Test
    void deleteSelfIs409() throws Exception {
        doThrow(new SelfDeletionException()).when(deleteUserUseCase).execute(ADMIN, 1L);

        mockMvc.perform(delete("/api/users/1").with(admin()))
                .andExpect(status().isConflict());
    }
}

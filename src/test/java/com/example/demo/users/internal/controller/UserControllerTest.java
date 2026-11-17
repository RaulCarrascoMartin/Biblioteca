package com.example.demo.users.internal.controller;

import com.example.demo.shared.Requester;
import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.handler.UserExceptionHandler;
import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.SelfDeletionException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.exception.UserWithLoansException;
import com.example.demo.users.internal.usecase.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba HTTP del controller en aislamiento: caso de uso simulado, sin Spring Security ni {@code config}. */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final Requester REQUESTER = new Requester(1L, Set.of("ADMIN"));

    @Mock
    private UserUseCase useCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver fixedRequester = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return Requester.class.equals(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return REQUESTER;
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(useCase))
                .setCustomArgumentResolvers(fixedRequester)
                .setControllerAdvice(new UserExceptionHandler())
                .build();
    }

    private static UserResponse ana() {
        return new UserResponse(2L, "Ana", "ana@x.es", "USER");
    }

    @Test
    void getAllReturnsUsersWithHistoricalFieldNamesAndNoPassword() throws Exception {
        when(useCase.getAll(REQUESTER)).thenReturn(List.of(ana()));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Ana"))
                .andExpect(jsonPath("$[0].rol").value("USER"))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        when(useCase.me(REQUESTER)).thenReturn(ana());

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@x.es"));
    }

    @Test
    void getByIdReturnsUser() throws Exception {
        when(useCase.getById(REQUESTER, 2L)).thenReturn(ana());

        mockMvc.perform(get("/api/usuarios/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getByIdUnknownIs404ProblemDetail() throws Exception {
        when(useCase.getById(REQUESTER, 9L)).thenThrow(new UserNotFoundException(9L));

        mockMvc.perform(get("/api/usuarios/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(containsString("9")))
                .andExpect(jsonPath("$.error").value(containsString("9")));
    }

    @Test
    void updateReturnsUpdatedUser() throws Exception {
        when(useCase.update(eq(REQUESTER), eq(2L), any())).thenReturn(ana());

        mockMvc.perform(put("/api/usuarios/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"email\":\"ana@x.es\",\"password\":\"Nueva4567\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void updateWithShortPasswordIs400ProblemDetailWithField() throws Exception {
        mockMvc.perform(put("/api/usuarios/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"email\":\"ana@x.es\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]").value(containsString("password")))
                .andExpect(jsonPath("$.error").value(containsString("8")));
    }

    @Test
    void updateWithInvalidEmailIs400() throws Exception {
        mockMvc.perform(put("/api/usuarios/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"email\":\"no-es-correo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("email"))));
    }

    @Test
    void updateWithRepeatedEmailKeepsHistoricalStatus400() throws Exception {
        when(useCase.update(eq(REQUESTER), eq(2L), any())).thenThrow(new EmailAlreadyRegisteredException());

        mockMvc.perform(put("/api/usuarios/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"email\":\"bob@x.es\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("registrado")));
    }

    @Test
    void revokeSessionsIs204() throws Exception {
        mockMvc.perform(post("/api/usuarios/2/revocar-sesiones"))
                .andExpect(status().isNoContent());

        verify(useCase).revokeSessions(REQUESTER, 2L);
    }

    @Test
    void deleteIs204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/2"))
                .andExpect(status().isNoContent());

        verify(useCase).delete(REQUESTER, 2L);
    }

    @Test
    void deleteUserWithLoansIs409() throws Exception {
        doThrow(new UserWithLoansException()).when(useCase).delete(REQUESTER, 2L);

        mockMvc.perform(delete("/api/usuarios/2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value(containsString("préstamos")));
    }

    @Test
    void deleteSelfIs409() throws Exception {
        doThrow(new SelfDeletionException()).when(useCase).delete(REQUESTER, 1L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isConflict());
    }
}

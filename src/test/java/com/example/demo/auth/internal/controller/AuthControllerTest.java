package com.example.demo.auth.internal.controller;

import com.example.demo.auth.internal.controller.dto.LoginDto;
import com.example.demo.auth.internal.controller.dto.RegisterDto;
import com.example.demo.auth.internal.controller.dto.RegisterResponse;
import com.example.demo.auth.internal.controller.handler.AuthExceptionHandler;
import com.example.demo.auth.internal.usecase.AuthUseCase;
import com.example.demo.shared.InvalidCredentialsException;
import com.example.demo.shared.Requester;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba HTTP del controller en aislamiento: caso de uso simulado, sin Spring Security ni {@code config}. */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final Requester REQUESTER = new Requester(1L, Set.of("USER"));

    @Mock
    private AuthUseCase useCase;

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
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(useCase))
                .setCustomArgumentResolvers(fixedRequester)
                .setControllerAdvice(new AuthExceptionHandler())
                .build();
    }

    private static String registerJson(String name, String email, String password) {
        return "{\"nombre\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(name, email, password);
    }

    @Test
    void registerReturnsUserWithHistoricalFieldNames() throws Exception {
        when(useCase.register(new RegisterDto("Ana", "ana@test.es", "Clave1234")))
                .thenReturn(Optional.of(new RegisterResponse(1L, "Ana", "ana@test.es", "USER")));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Ana", "ana@test.es", "Clave1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@test.es"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void registerWithDuplicateEmailReturns200WithEmptyBody() throws Exception {
        when(useCase.register(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Ana", "dup@test.es", "Clave1234")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void registerWithShortPasswordReturns400ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("A", "short@test.es", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(containsString("password"))))
                .andExpect(jsonPath("$.error").value(containsString("8")));

        verify(useCase, never()).register(any());
    }

    @Test
    void registerWithInvalidEmailReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("A", "no-es-correo", "Clave1234")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsTokenInBodyAndHttpOnlyCookie() throws Exception {
        when(useCase.login(new LoginDto("lex@test.es", "Pass1234"))).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"lex@test.es\",\"password\":\"Pass1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt_token=jwt-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        when(useCase.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@test.es\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @Test
    void loginWithoutFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(useCase, never()).login(any());
    }

    @Test
    void logoutRevokesHeaderAndCookieTokensAndClearsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer h-token")
                        .cookie(new Cookie("jwt_token", "c-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(useCase).logout(REQUESTER, List.of("h-token", "c-token"));
    }

    @Test
    void logoutIgnoresNonBearerAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, "Basic abc"))
                .andExpect(status().isOk());

        verify(useCase).logout(REQUESTER, List.of());
    }
}

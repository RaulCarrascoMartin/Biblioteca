package com.example.demo.auth.internal.controller;

import com.example.demo.auth.internal.controller.mapper.AuthWebMapperImpl;
import com.example.demo.auth.internal.domain.dto.RegisterUserDto;
import com.example.demo.auth.internal.domain.dto.RegisteredUserDto;
import com.example.demo.auth.internal.domain.dto.TokenPairDto;
import com.example.demo.auth.internal.usecase.LoginUseCase;
import com.example.demo.auth.internal.usecase.LogoutUseCase;
import com.example.demo.auth.internal.usecase.RefreshTokenUseCase;
import com.example.demo.auth.internal.usecase.RegisterUseCase;
import com.example.demo.config.security.RateLimitFilter;
import com.example.demo.config.security.SecurityConfig;
import com.example.demo.shared.AuthenticatedPrincipal;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.InvalidCredentialsException;
import com.example.demo.shared.Requester;
import com.example.demo.shared.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.example.demo.support.TestAuthentication.asUser;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP con la {@code SecurityConfig} real: rutas públicas, 401 sin identidad, 403 y traducción de errores. */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthWebMapperImpl.class})
class AuthControllerTest {

    private static final Requester USER = new Requester(1L, Set.of("USER"));
    private static final String LOGIN_BODY = "{\"email\":\"lex@test.es\",\"password\":\"Pass1234\"}";
    private static final String REFRESH_BODY = "{\"refreshToken\":\"r-token\"}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RateLimitFilter rateLimitFilter;

    @MockitoBean
    private RegisterUseCase registerUseCase;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;
    // Colaborador del filtro JWT de la SecurityConfig real
    @MockitoBean
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        rateLimitFilter.reset();
    }

    private static String registerJson(String name, String email, String password) {
        return "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(name, email, password);
    }

    // ---- rutas públicas

    @Test
    void registerLoginAndRefreshArePublic() throws Exception {
        when(registerUseCase.execute(any())).thenReturn(Optional.empty());
        when(loginUseCase.execute("lex@test.es", "Pass1234")).thenReturn(new TokenPairDto("a", "r"));
        when(refreshTokenUseCase.execute("r-token")).thenReturn(new TokenPairDto("a2", "r2"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Ana", "ana@test.es", "Clave1234")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void logoutWithoutIdentityIs401Json() throws Exception {
        mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autenticado"));

        verifyNoInteractions(registerUseCase, loginUseCase, refreshTokenUseCase, logoutUseCase);
    }

    @Test
    void invalidBearerTokenIs401() throws Exception {
        when(tokenProvider.validate("basura")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer basura")
                        .contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validBearerTokenOpensLogoutAndBuildsTheRequesterFromTheToken() throws Exception {
        when(tokenProvider.validate("jwt")).thenReturn(Optional.of(new AuthenticatedPrincipal(1L, List.of("ROLE_USER"))));

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer jwt")
                        .contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada"));

        verify(logoutUseCase).execute(USER, "r-token");
    }

    @Test
    void forbiddenFromTheUseCaseIs403() throws Exception {
        org.mockito.Mockito.doThrow(new ForbiddenException()).when(logoutUseCase).execute(USER, "r-token");

        mockMvc.perform(post("/api/auth/logout").with(asUser(1L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isForbidden());
    }

    // ---- register

    @Test
    void registerReturnsUserWithHistoricalFieldNames() throws Exception {
        when(registerUseCase.execute(new RegisterUserDto("Ana", "ana@test.es", "Clave1234")))
                .thenReturn(Optional.of(new RegisteredUserDto(1L, "Ana", "ana@test.es", "USER")));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Ana", "ana@test.es", "Clave1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@test.es"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void registerWithDuplicateEmailReturns200WithEmptyBody() throws Exception {
        when(registerUseCase.execute(any())).thenReturn(Optional.empty());

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

        verify(registerUseCase, never()).execute(any());
    }

    @Test
    void registerWithInvalidEmailReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("A", "no-es-correo", "Clave1234")))
                .andExpect(status().isBadRequest());
    }

    // ---- login

    @Test
    void loginReturnsTheTokenPairAndNoCookie() throws Exception {
        when(loginUseCase.execute("lex@test.es", "Pass1234")).thenReturn(new TokenPairDto("jwt", "opaque"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"))
                .andExpect(jsonPath("$.refreshToken").value("opaque"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().doesNotExist("Set-Cookie"));
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        when(loginUseCase.execute(any(), any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@test.es\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @Test
    void loginWithoutFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(loginUseCase, never()).execute(any(), any());
    }

    // ---- refresh

    @Test
    void refreshReturnsTheNewPair() throws Exception {
        when(refreshTokenUseCase.execute("r-token")).thenReturn(new TokenPairDto("jwt2", "opaque2"));

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt2"))
                .andExpect(jsonPath("$.refreshToken").value("opaque2"));
    }

    @Test
    void refreshWithInvalidTokenReturns401() throws Exception {
        when(refreshTokenUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithoutTokenReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(refreshTokenUseCase, never()).execute(any());
    }

    // ---- logout

    @Test
    void logoutWithoutRefreshTokenReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(asUser(1L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}

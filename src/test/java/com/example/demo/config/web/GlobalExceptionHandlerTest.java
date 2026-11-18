package com.example.demo.config.web;

import com.example.demo.shared.ForbiddenException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** El formato ProblemDetail del manejador global se prueba aquí una sola vez. */
class GlobalExceptionHandlerTest {

    record Body(@NotBlank(message = "es obligatorio") String name,
                @Size(min = 8, message = "mínimo 8") String password) {
    }

    @RestController
    static class ProbeController {
        @PostMapping("/probe/body")
        void body(@Valid @RequestBody Body body) {
        }

        @GetMapping("/probe/forbidden")
        void forbidden() {
            throw new ForbiddenException();
        }

        @GetMapping("/probe/denied")
        void denied() {
            throw new AccessDeniedException("secreto interno");
        }

        @GetMapping("/probe/unauthenticated")
        void unauthenticated() {
            throw new BadCredentialsException("x");
        }

        @GetMapping("/probe/boom")
        void boom() {
            throw new RuntimeException("detalle SQL interno");
        }

        @GetMapping("/probe/param")
        void param(@RequestParam("n") int n) {
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void invalidBodyIs400WithFieldAndReason() throws Exception {
        mockMvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem("name: es obligatorio")))
                .andExpect(jsonPath("$.errors", hasItem("password: mínimo 8")))
                .andExpect(jsonPath("$.error").value(containsString("name: es obligatorio")))
                .andExpect(jsonPath("$.detail").value(containsString("password: mínimo 8")));
    }

    @Test
    void malformedBodyIs400() throws Exception {
        mockMvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON).content("{no json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cuerpo de la petición inválido"));
    }

    @Test
    void forbiddenExceptionIs403ProblemDetail() throws Exception {
        mockMvc.perform(get("/probe/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("No tienes permiso para realizar esta acción"))
                .andExpect(jsonPath("$.error").value("No tienes permiso para realizar esta acción"));
    }

    @Test
    void springAccessDeniedIs403WithoutLeakingItsMessage() throws Exception {
        mockMvc.perform(get("/probe/denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", not(containsString("secreto"))));
    }

    @Test
    void authenticationExceptionIs401() throws Exception {
        mockMvc.perform(get("/probe/unauthenticated"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autenticado"));
    }

    @Test
    void unexpectedExceptionIsGeneric500WithoutDetails() throws Exception {
        mockMvc.perform(get("/probe/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"))
                .andExpect(content().string(not(containsString("SQL"))));
    }

    @Test
    void typeMismatchAndMissingParamAre400() throws Exception {
        mockMvc.perform(get("/probe/param?n=abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parámetro inválido: n"));
        mockMvc.perform(get("/probe/param")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parámetro requerido faltante: n"));
    }

    @Test
    void springMvcErrorsKeepTheirStatus() throws Exception {
        mockMvc.perform(post("/probe/forbidden"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").exists());
    }
}

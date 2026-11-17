package com.example.demo.catalog.internal.controller;

import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.exception.BookNotFoundException;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.catalog.internal.controller.handler.BookExceptionHandler;
import com.example.demo.catalog.internal.domain.exception.BookWithLoansException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.usecase.BookUseCase;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** MockMvc standalone: sin contexto Spring, sin seguridad ni {@code config}; el caso de uso va simulado. */
class BookControllerTest {

    private static final Requester REQUESTER = new Requester(1L, Set.of("ADMIN"));
    private static final BookResponse RESPONSE = new BookResponse(5L, "Dune", "Frank Herbert", "9780441013593", true, Genre.CIENCIA_FICCION);
    private static final String VALID_BODY =
            "{\"titulo\":\"Dune\",\"autor\":\"Frank Herbert\",\"isbn\":\"9780441013593\",\"genero\":\"CIENCIA_FICCION\"}";

    private BookUseCase useCase;
    private MockMvc mockMvc;

    /** Resolver de test: entrega siempre el mismo Requester (el real lo escribe {@code config}). */
    private static class FixedRequesterResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return Requester.class.equals(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return REQUESTER;
        }
    }

    @BeforeEach
    void setUp() {
        useCase = mock(BookUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new BookController(useCase))
                .setCustomArgumentResolvers(new FixedRequesterResolver())
                .setControllerAdvice(new BookExceptionHandler())
                .build();
    }

    @Test
    void getAll_returnsJsonArrayWithSpanishFieldNames() throws Exception {
        when(useCase.getAll(REQUESTER)).thenReturn(List.of(RESPONSE));

        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].titulo").value("Dune"))
                .andExpect(jsonPath("$[0].autor").value("Frank Herbert"))
                .andExpect(jsonPath("$[0].disponible").value(true))
                .andExpect(jsonPath("$[0].genero").value("CIENCIA_FICCION"));
    }

    @Test
    void getById_existing_200() throws Exception {
        when(useCase.getById(REQUESTER, 5L)).thenReturn(RESPONSE);

        mockMvc.perform(get("/api/libros/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("9780441013593"));
    }

    @Test
    void getById_missing_404ProblemDetail() throws Exception {
        when(useCase.getById(REQUESTER, 9999L)).thenThrow(new BookNotFoundException());

        mockMvc.perform(get("/api/libros/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Libro no encontrado"));
    }

    @Test
    void create_valid_returns200WithBook_andPassesRequester() throws Exception {
        when(useCase.create(eq(REQUESTER), any())).thenReturn(RESPONSE);

        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));

        verify(useCase).create(eq(REQUESTER), any());
    }

    @Test
    void create_emptyBody_400ProblemDetailWithFieldAndReason() throws Exception {
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.detail").value(containsString("título")))
                .andExpect(jsonPath("$.detail").value(containsString("ISBN")));
    }

    @Test
    void create_shortIsbn_400() throws Exception {
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"T\",\"autor\":\"A\",\"isbn\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("entre 10 y 20")));
    }

    @Test
    void create_duplicateIsbn_409ProblemDetail() throws Exception {
        when(useCase.create(eq(REQUESTER), any())).thenThrow(new DuplicateIsbnException());

        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("El ISBN ya existe en el catálogo"));
    }

    @Test
    void update_missing_404() throws Exception {
        when(useCase.update(eq(REQUESTER), eq(9999L), any())).thenThrow(new BookNotFoundException());

        mockMvc.perform(put("/api/libros/9999").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_isbnOfAnotherBook_409() throws Exception {
        when(useCase.update(eq(REQUESTER), eq(5L), any())).thenThrow(new DuplicateIsbnException());

        mockMvc.perform(put("/api/libros/5").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void update_valid_200() throws Exception {
        when(useCase.update(eq(REQUESTER), eq(5L), any())).thenReturn(RESPONSE);

        mockMvc.perform(put("/api/libros/5").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Dune"));
    }

    @Test
    void delete_existing_204() throws Exception {
        mockMvc.perform(delete("/api/libros/5")).andExpect(status().isNoContent());

        verify(useCase).delete(REQUESTER, 5L);
    }

    @Test
    void delete_missing_404() throws Exception {
        org.mockito.Mockito.doThrow(new BookNotFoundException()).when(useCase).delete(REQUESTER, 9999L);

        mockMvc.perform(delete("/api/libros/9999")).andExpect(status().isNotFound());
    }

    @Test
    void delete_withLoans_409() throws Exception {
        org.mockito.Mockito.doThrow(new BookWithLoansException()).when(useCase).delete(REQUESTER, 5L);

        mockMvc.perform(delete("/api/libros/5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede retirar un libro con préstamos registrados"));
    }

    @Test
    void forbidden_isNotTranslatedByTheModuleHandler() {
        // El 403 lo traduce el handler global (shared/config); aquí solo se comprueba que la excepción
        // del caso de uso llega al exterior sin que el handler del módulo la capture.
        when(useCase.getAll(REQUESTER)).thenThrow(new ForbiddenException());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> mockMvc.perform(get("/api/libros")));
    }
}

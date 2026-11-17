package com.example.demo.loans.internal.controller;

import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.exception.BookNotAvailableException;
import com.example.demo.catalog.exception.BookNotFoundException;
import com.example.demo.loans.internal.controller.dto.LoanBookResponse;
import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.loans.internal.controller.handler.LoanExceptionHandler;
import com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.usecase.LoanUseCase;
import com.example.demo.shared.Requester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba HTTP del controller en aislamiento: caso de uso simulado, sin Spring Security ni {@code config}. */
@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    private static final Requester REQUESTER = new Requester(7L, Set.of("USER"));

    @Mock
    private LoanUseCase useCase;

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
        mockMvc = MockMvcBuilders.standaloneSetup(new LoanController(useCase))
                .setCustomArgumentResolvers(fixedRequester)
                .setControllerAdvice(new LoanExceptionHandler())
                .build();
    }

    private static LoanResponse loan(Long id) {
        return new LoanResponse(id, LocalDate.of(2026, 1, 2), null,
                new LoanBookResponse(3L, "Libro A", "Autor", "123", false, Genre.CLASICA), 7L, "Ana", "ana@x.es");
    }

    @Test
    void getAllReturnsLoansWithHistoricalFieldNames() throws Exception {
        when(useCase.getAll(REQUESTER)).thenReturn(List.of(loan(1L)));

        mockMvc.perform(get("/api/prestamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fechaPrestamo").value("2026-01-02"))
                .andExpect(jsonPath("$[0].fechaDevolucion").isEmpty())
                .andExpect(jsonPath("$[0].libro.titulo").value("Libro A"))
                .andExpect(jsonPath("$[0].libro.disponible").value(false))
                .andExpect(jsonPath("$[0].usuarioId").value(7))
                .andExpect(jsonPath("$[0].usuarioNombre").value("Ana"))
                .andExpect(jsonPath("$[0].usuarioEmail").value("ana@x.es"));
    }

    @Test
    void getOwnReturnsTheRequestersLoans() throws Exception {
        when(useCase.getOwn(REQUESTER)).thenReturn(List.of(loan(1L)));

        mockMvc.perform(get("/api/prestamos/mios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByIdReturnsTheLoan() throws Exception {
        when(useCase.getById(REQUESTER, 1L)).thenReturn(loan(1L));

        mockMvc.perform(get("/api/prestamos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByIdUnknownIs404WithErrorProperty() throws Exception {
        when(useCase.getById(REQUESTER, 9L)).thenThrow(new LoanNotFoundException());

        mockMvc.perform(get("/api/prestamos/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Préstamo no encontrado"))
                .andExpect(jsonPath("$.detail").value("Préstamo no encontrado"));
    }

    @Test
    void createPassesTheBookIdAndReturns200() throws Exception {
        when(useCase.create(REQUESTER, 3L)).thenReturn(loan(1L));

        mockMvc.perform(post("/api/prestamos").param("libroId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libro.id").value(3));
    }

    @Test
    void createWithUnknownBookIs404() throws Exception {
        when(useCase.create(REQUESTER, 99L)).thenThrow(new BookNotFoundException());

        mockMvc.perform(post("/api/prestamos").param("libroId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithUnavailableBookIs409() throws Exception {
        when(useCase.create(REQUESTER, 3L)).thenThrow(new BookNotAvailableException());

        mockMvc.perform(post("/api/prestamos").param("libroId", "3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createWithNonPositiveBookIdIs400() throws Exception {
        mockMvc.perform(post("/api/prestamos").param("libroId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(useCase);
    }

    @Test
    void createWithoutBookIdIs400() throws Exception {
        mockMvc.perform(post("/api/prestamos"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    void returnLoanReturns200() throws Exception {
        when(useCase.returnLoan(REQUESTER, 1L)).thenReturn(loan(1L));

        mockMvc.perform(put("/api/prestamos/1/devolver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void returnLoanAlreadyReturnedIs409() throws Exception {
        when(useCase.returnLoan(REQUESTER, 1L)).thenThrow(new LoanAlreadyReturnedException());

        mockMvc.perform(put("/api/prestamos/1/devolver"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El préstamo ya fue devuelto"));
    }
}

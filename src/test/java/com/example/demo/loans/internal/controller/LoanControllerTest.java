package com.example.demo.loans.internal.controller;

import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.config.SecurityConfig;
import com.example.demo.loans.internal.controller.mapper.LoanWebMapperImpl;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.exception.InvalidLoanException;
import com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotFoundException;
import com.example.demo.loans.internal.domain.exception.LoanConcurrentModificationException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.usecase.CreateLoanUseCase;
import com.example.demo.loans.internal.usecase.GetAllLoansUseCase;
import com.example.demo.loans.internal.usecase.GetLoanUseCase;
import com.example.demo.loans.internal.usecase.GetOwnLoansUseCase;
import com.example.demo.loans.internal.usecase.ReturnLoanUseCase;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.AuthenticatedPrincipal;
import com.example.demo.shared.Requester;
import com.example.demo.shared.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.example.demo.support.TestAuthentication.asUser;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba HTTP del controller con la {@code SecurityConfig} real: 401 sin identidad, 403 cuando el caso de
 * uso (simulado) deniega, contrato y traducción de errores. La política de acceso se prueba en
 * los tests de los casos de uso.
 */
@WebMvcTest(LoanController.class)
@Import({SecurityConfig.class, LoanWebMapperImpl.class})
class LoanControllerTest {

    private static final Requester REQUESTER = new Requester(7L, Set.of("USER"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAllLoansUseCase getAllLoansUseCase;

    @MockitoBean
    private GetOwnLoansUseCase getOwnLoansUseCase;

    @MockitoBean
    private GetLoanUseCase getLoanUseCase;

    @MockitoBean
    private CreateLoanUseCase createLoanUseCase;

    @MockitoBean
    private ReturnLoanUseCase returnLoanUseCase;
    // Colaborador del filtro JWT de la SecurityConfig real
    @MockitoBean
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        when(tokenProvider.validate("jwt"))
                .thenReturn(java.util.Optional.of(new AuthenticatedPrincipal(7L, List.of("ROLE_USER"))));
    }

    /** Token Bearer de Ana (id 7, rol USER) que valida el {@code TokenProvider} simulado. */
    private static RequestPostProcessor ana() {
        return request -> {
            request.addHeader("Authorization", "Bearer jwt");
            return request;
        };
    }

    // ---- 401 / 403

    @Test
    void everyEndpointIs401WithoutIdentity() throws Exception {
        mockMvc.perform(get("/api/prestamos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/prestamos/mios")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/prestamos/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/prestamos").param("libroId", "3")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/prestamos/1/devolver")).andExpect(status().isUnauthorized());

        verifyNoInteractions(getAllLoansUseCase, getOwnLoansUseCase, getLoanUseCase, createLoanUseCase, returnLoanUseCase);
    }

    @Test
    void forbiddenFromUseCaseIs403() throws Exception {
        when(getAllLoansUseCase.execute(REQUESTER)).thenThrow(new ForbiddenException());
        when(getLoanUseCase.execute(REQUESTER, 1L)).thenThrow(new ForbiddenException());

        mockMvc.perform(get("/api/prestamos").with(ana())).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/prestamos/1").with(ana())).andExpect(status().isForbidden());
    }

    // ---- éxito y contrato

    private static LoanDto loan(Long id) {
        return new LoanDto(id, LocalDate.of(2026, 1, 2), null,
                new BookSummary(3L, "Libro A", "Autor", "123", false, Genre.CLASICA), 7L, "Ana", "ana@x.es");
    }

    @Test
    void getAllReturnsLoansWithHistoricalFieldNames() throws Exception {
        when(getAllLoansUseCase.execute(REQUESTER)).thenReturn(List.of(loan(1L)));

        mockMvc.perform(get("/api/prestamos").with(ana()))
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
        when(getOwnLoansUseCase.execute(REQUESTER)).thenReturn(List.of(loan(1L)));

        mockMvc.perform(get("/api/prestamos/mios").with(ana()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByIdReturnsTheLoan() throws Exception {
        when(getLoanUseCase.execute(REQUESTER, 1L)).thenReturn(loan(1L));

        mockMvc.perform(get("/api/prestamos/1").with(ana()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByIdUnknownIs404WithErrorProperty() throws Exception {
        when(getLoanUseCase.execute(REQUESTER, 9L)).thenThrow(new LoanNotFoundException());

        mockMvc.perform(get("/api/prestamos/9").with(ana()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Préstamo no encontrado"))
                .andExpect(jsonPath("$.detail").value("Préstamo no encontrado"));
    }

    @Test
    void createPassesTheBookIdAndReturns201WithLocation() throws Exception {
        when(createLoanUseCase.execute(REQUESTER, 3L)).thenReturn(loan(1L));

        mockMvc.perform(post("/api/prestamos").with(ana()).param("libroId", "3"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/prestamos/1"))
                .andExpect(jsonPath("$.libro.id").value(3));
    }

    @Test
    void createWithUnknownBookIs404() throws Exception {
        when(createLoanUseCase.execute(REQUESTER, 99L)).thenThrow(new LoanBookNotFoundException());

        mockMvc.perform(post("/api/prestamos").with(ana()).param("libroId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithUnavailableBookIs409() throws Exception {
        when(createLoanUseCase.execute(REQUESTER, 3L)).thenThrow(new LoanBookNotAvailableException());

        mockMvc.perform(post("/api/prestamos").with(ana()).param("libroId", "3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createWithNonPositiveBookIdIs400() throws Exception {
        mockMvc.perform(post("/api/prestamos").with(ana()).param("libroId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(getAllLoansUseCase, getOwnLoansUseCase, getLoanUseCase, createLoanUseCase, returnLoanUseCase);
    }

    @Test
    void createWithoutBookIdIs400() throws Exception {
        mockMvc.perform(post("/api/prestamos").with(ana()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getAllLoansUseCase, getOwnLoansUseCase, getLoanUseCase, createLoanUseCase, returnLoanUseCase);
    }

    @Test
    void returnLoanReturns200() throws Exception {
        when(returnLoanUseCase.execute(REQUESTER, 1L)).thenReturn(loan(1L));

        mockMvc.perform(put("/api/prestamos/1/devolver").with(ana()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void returnLoanAlreadyReturnedIs409() throws Exception {
        when(returnLoanUseCase.execute(REQUESTER, 1L)).thenThrow(new LoanAlreadyReturnedException());

        mockMvc.perform(put("/api/prestamos/1/devolver").with(ana()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El préstamo ya fue devuelto"));
    }

    @Test
    void returnLoanConcurrentModificationIs409() throws Exception {
        when(returnLoanUseCase.execute(REQUESTER, 1L)).thenThrow(new LoanConcurrentModificationException(null));

        mockMvc.perform(put("/api/prestamos/1/devolver").with(ana()))
                .andExpect(status().isConflict());
    }

    @Test
    void createInvalidLoanIs422() throws Exception {
        when(createLoanUseCase.execute(REQUESTER, 3L)).thenThrow(new InvalidLoanException("El libro es obligatorio"));

        mockMvc.perform(post("/api/prestamos").with(ana()).param("libroId", "3"))
                .andExpect(status().isUnprocessableEntity());
    }
}

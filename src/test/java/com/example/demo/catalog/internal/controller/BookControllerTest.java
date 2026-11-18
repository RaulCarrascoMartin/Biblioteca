package com.example.demo.catalog.internal.controller;

import com.example.demo.catalog.internal.controller.mapper.BookWebMapperImpl;
import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.exception.BookWithLoansException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.domain.exception.InvalidBookException;
import com.example.demo.catalog.internal.domain.model.Genre;
import com.example.demo.catalog.internal.usecase.CreateBookUseCase;
import com.example.demo.catalog.internal.usecase.DeleteBookUseCase;
import com.example.demo.catalog.internal.usecase.GetAllBooksUseCase;
import com.example.demo.catalog.internal.usecase.GetBookUseCase;
import com.example.demo.catalog.internal.usecase.UpdateBookUseCase;
import com.example.demo.config.security.SecurityConfig;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.shared.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Set;

import static com.example.demo.support.TestAuthentication.asUser;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba HTTP del controller con la {@code SecurityConfig} real: 401 sin identidad, 403 cuando el caso de
 * uso (simulado) deniega, contrato de éxito y traducción de errores. La política de acceso se prueba en
 * los tests de los casos de uso.
 */
@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, BookWebMapperImpl.class})
class BookControllerTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester BOB = new Requester(3L, Set.of("USER"));
    private static final BookDto DTO = new BookDto(5L, "Dune", "Frank Herbert", "9780441013593", true,
            Genre.CIENCIA_FICCION);
    private static final String VALID_BODY =
            "{\"title\":\"Dune\",\"author\":\"Frank Herbert\",\"isbn\":\"9780441013593\",\"genre\":\"CIENCIA_FICCION\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAllBooksUseCase getAllBooksUseCase;

    @MockitoBean
    private GetBookUseCase getBookUseCase;

    @MockitoBean
    private CreateBookUseCase createBookUseCase;

    @MockitoBean
    private UpdateBookUseCase updateBookUseCase;

    @MockitoBean
    private DeleteBookUseCase deleteBookUseCase;
    // Colaborador del filtro JWT de la SecurityConfig real (la identidad se publica con TestAuthentication)
    @MockitoBean
    private TokenProvider tokenProvider;

    private static RequestPostProcessor admin() {
        return asUser(1L, "ADMIN");
    }

    private static RequestPostProcessor bob() {
        return asUser(3L, "USER");
    }

    // ---- 401 / 403

    @Test
    void everyEndpointIs401WithoutIdentity() throws Exception {
        mockMvc.perform(get("/api/libros")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/libros/5")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/libros/5").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/libros/5")).andExpect(status().isUnauthorized());

        verifyNoInteractions(getAllBooksUseCase, getBookUseCase, createBookUseCase, updateBookUseCase, deleteBookUseCase);
    }

    @Test
    void forbiddenFromUseCaseIs403() throws Exception {
        when(createBookUseCase.execute(eq(BOB), any(SaveBookDto.class))).thenThrow(new ForbiddenException());
        when(updateBookUseCase.execute(eq(BOB), eq(5L), any(SaveBookDto.class))).thenThrow(new ForbiddenException());
        doThrow(new ForbiddenException()).when(deleteBookUseCase).execute(BOB, 5L);

        mockMvc.perform(post("/api/libros").with(bob()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/libros/5").with(bob()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/libros/5").with(bob())).andExpect(status().isForbidden());
    }

    // ---- éxito y contrato

    @Test
    void getAll_returnsJsonArrayWithSpanishFieldNames() throws Exception {
        when(getAllBooksUseCase.execute(BOB)).thenReturn(List.of(DTO));

        mockMvc.perform(get("/api/libros").with(bob()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$[0].author").value("Frank Herbert"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].genre").value("CIENCIA_FICCION"));
    }

    @Test
    void getById_existing_200() throws Exception {
        when(getBookUseCase.execute(BOB, 5L)).thenReturn(DTO);

        mockMvc.perform(get("/api/libros/5").with(bob()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("9780441013593"));
    }

    @Test
    void getById_missing_404ProblemDetail() throws Exception {
        when(getBookUseCase.execute(BOB, 9999L)).thenThrow(new BookNotFoundException());

        mockMvc.perform(get("/api/libros/9999").with(bob()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Libro no encontrado"));
    }

    @Test
    void create_valid_returns201WithLocationAndBook() throws Exception {
        when(createBookUseCase.execute(eq(ADMIN), any(SaveBookDto.class))).thenReturn(DTO);

        mockMvc.perform(post("/api/libros").with(admin()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/libros/5"))
                .andExpect(jsonPath("$.available").value(true));

        verify(createBookUseCase).execute(ADMIN, new SaveBookDto("Dune", "Frank Herbert", "9780441013593", Genre.CIENCIA_FICCION));
    }

    @Test
    void create_emptyBody_400ProblemDetailWithFieldAndReason() throws Exception {
        mockMvc.perform(post("/api/libros").with(admin()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.detail").value(containsString("título")))
                .andExpect(jsonPath("$.detail").value(containsString("ISBN")));
    }

    @Test
    void create_shortIsbn_400() throws Exception {
        mockMvc.perform(post("/api/libros").with(admin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\",\"author\":\"A\",\"isbn\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("entre 10 y 20")));
    }

    @Test
    void create_duplicateIsbn_409ProblemDetail() throws Exception {
        when(createBookUseCase.execute(eq(ADMIN), any(SaveBookDto.class))).thenThrow(new DuplicateIsbnException());

        mockMvc.perform(post("/api/libros").with(admin()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("El ISBN ya existe en el catálogo"));
    }

    @Test
    void create_domainInvariantViolation_422() throws Exception {
        when(createBookUseCase.execute(eq(ADMIN), any(SaveBookDto.class)))
                .thenThrow(new InvalidBookException("El género es obligatorio"));

        mockMvc.perform(post("/api/libros").with(admin()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("El género es obligatorio"));
    }

    @Test
    void update_missing_404() throws Exception {
        when(updateBookUseCase.execute(eq(ADMIN), eq(9999L), any(SaveBookDto.class))).thenThrow(new BookNotFoundException());

        mockMvc.perform(put("/api/libros/9999").with(admin()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_isbnOfAnotherBook_409() throws Exception {
        when(updateBookUseCase.execute(eq(ADMIN), eq(5L), any(SaveBookDto.class))).thenThrow(new DuplicateIsbnException());

        mockMvc.perform(put("/api/libros/5").with(admin()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void update_valid_200() throws Exception {
        when(updateBookUseCase.execute(eq(ADMIN), eq(5L), any(SaveBookDto.class))).thenReturn(DTO);

        mockMvc.perform(put("/api/libros/5").with(admin()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"));
    }

    @Test
    void delete_existing_204() throws Exception {
        mockMvc.perform(delete("/api/libros/5").with(admin())).andExpect(status().isNoContent());

        verify(deleteBookUseCase).execute(ADMIN, 5L);
    }

    @Test
    void delete_missing_404() throws Exception {
        doThrow(new BookNotFoundException()).when(deleteBookUseCase).execute(ADMIN, 9999L);

        mockMvc.perform(delete("/api/libros/9999").with(admin())).andExpect(status().isNotFound());
    }

    @Test
    void delete_withLoans_409() throws Exception {
        doThrow(new BookWithLoansException()).when(deleteBookUseCase).execute(ADMIN, 5L);

        mockMvc.perform(delete("/api/libros/5").with(admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede retirar un libro con préstamos registrados"));
    }
}

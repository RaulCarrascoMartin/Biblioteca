package com.example.demo.catalog.internal.controller.handler;

import com.example.demo.catalog.internal.domain.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotFoundException;
import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.domain.exception.BookWithLoansException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.domain.exception.InvalidBookException;
import com.example.demo.catalog.internal.domain.exception.InvalidIsbnException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookExceptionHandlerTest {

    private final BookExceptionHandler handler = new BookExceptionHandler();

    @Test
    void notFound_is404WithDetail() {
        ProblemDetail problem = handler.handleNotFound(new BookNotFoundException());

        assertEquals(404, problem.getStatus());
        assertEquals("Libro no encontrado", problem.getDetail());
    }

    @Test
    void conflicts_are409() {
        assertEquals(409, handler.handleConflict(new DuplicateIsbnException()).getStatus());
        assertEquals(409, handler.handleConflict(new BookWithLoansException()).getStatus());
        assertEquals(409, handler.handleConflict(new BookNotAvailableException()).getStatus());
        assertEquals(409, handler.handleConflict(new BookAlreadyAvailableException()).getStatus());
    }

    @Test
    void invariants_are422() {
        assertEquals(422, handler.handleInvalid(new InvalidBookException("El género es obligatorio")).getStatus());
        assertEquals(422, handler.handleInvalid(new InvalidIsbnException("1")).getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void validation_is400WithFieldAndReason() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(
                new BookRequest(null, "A", "1234567890", null), "bookRequest");
        binding.addError(new org.springframework.validation.FieldError(
                "bookRequest", "title", "El título es obligatorio"));
        MethodParameter parameter = new MethodParameter(
                BookExceptionHandlerTest.class.getDeclaredMethod("dummy", BookRequest.class), 0);

        ProblemDetail problem = handler.handleValidation(new MethodArgumentNotValidException(parameter, binding));

        assertEquals(400, problem.getStatus());
        List<String> errors = (List<String>) problem.getProperties().get("errors");
        assertEquals(List.of("title: El título es obligatorio"), errors);
        assertTrue(problem.getDetail().contains("title"));
    }

    @SuppressWarnings("unused")
    void dummy(BookRequest request) {
    }
}

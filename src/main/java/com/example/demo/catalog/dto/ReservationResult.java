package com.example.demo.catalog.dto;

/**
 * Resultado de reservar o liberar un libro. {@code book} solo viene informado cuando el estado es
 * {@link Status#OK}.
 */
public record ReservationResult(Status status, BookSummary book) {

    public enum Status {
        OK,
        NOT_FOUND,
        /** Reservar: el libro ya está prestado. */
        NOT_AVAILABLE,
        /** Liberar: el libro ya estaba disponible. */
        ALREADY_AVAILABLE
    }

    public static ReservationResult ok(BookSummary book) {
        return new ReservationResult(Status.OK, book);
    }

    public static ReservationResult of(Status status) {
        return new ReservationResult(status, null);
    }
}

package com.example.demo.users.internal.domain.exception;

/** No existe el usuario pedido. */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuario %d no encontrado".formatted(id));
    }
}

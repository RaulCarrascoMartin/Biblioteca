package com.example.demo.shared;

/** Datos que identifican a una persona dentro de un token ya validado. */
public record TokenClaims(String email, int version) {
}

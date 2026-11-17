package com.example.demo.shared;

/** Puerto técnico: lista de tokens revocados (cierre de sesión). */
public interface TokenRevocation {

    void revoke(String token);

    boolean isRevoked(String token);
}

package com.example.demo.auth.internal.domain;

public interface TokenRevocation {
    void revoke(String token);
}

package com.example.demo.auth.domain;

public interface TokenRevocation {
    void revoke(String token);
}

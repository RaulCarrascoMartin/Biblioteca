package com.example.demo.domain.port;

public interface TokenRevocation {
    void revoke(String token);
}

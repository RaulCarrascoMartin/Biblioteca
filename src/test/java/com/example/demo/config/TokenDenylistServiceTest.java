package com.example.demo.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenDenylistServiceTest {

    private final TokenDenylistService service = new TokenDenylistService();

    @Test
    void revokedTokenIsReportedAsRevoked() {
        service.revoke("h.p.sig1");
        assertTrue(service.isRevoked("h.p.sig1"));
    }

    @Test
    void otherTokensAreNotRevoked() {
        service.revoke("h.p.sig1");
        assertFalse(service.isRevoked("h.p.sig2"));
    }

    @Test
    void revocationIsBySignature() {
        service.revoke("h.p.sig1");
        assertTrue(service.isRevoked("otro.payload.sig1"));
    }

    @Test
    void malformedOrNullTokensAreIgnored() {
        service.revoke(null);
        service.revoke("sin-puntos");
        assertFalse(service.isRevoked(null));
        assertFalse(service.isRevoked("sin-puntos"));
    }

    @Test
    void evictExpiredKeepsActiveRevocations() {
        service.revoke("h.p.sig1");
        service.evictExpired();
        assertTrue(service.isRevoked("h.p.sig1"));
    }
}

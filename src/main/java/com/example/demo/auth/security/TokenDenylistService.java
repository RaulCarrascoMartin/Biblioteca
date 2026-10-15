package com.example.demo.auth.security;

import com.example.demo.auth.domain.TokenRevocation;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenDenylistService implements TokenRevocation {

    private final Map<String, Instant> denylist = new ConcurrentHashMap<>();

    @Override
    public void revoke(String token) {
        String sig = signature(token);
        if (sig != null) {
            if (denylist.size() > 1000) {
                evictExpired();
            }
            denylist.put(sig, Instant.now().plusSeconds(1800));
        }
    }

    public boolean isRevoked(String token) {
        String sig = signature(token);
        if (sig == null) return false;
        Instant expiry = denylist.get(sig);
        if (expiry == null) return false;
        if (Instant.now().isAfter(expiry)) {
            denylist.remove(sig);
            return false;
        }
        return true;
    }

    private String signature(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        return parts.length == 3 ? parts[2] : null;
    }

    public void evictExpired() {
        Iterator<Map.Entry<String, Instant>> it = denylist.entrySet().iterator();
        while (it.hasNext()) {
            if (Instant.now().isAfter(it.next().getValue())) {
                it.remove();
            }
        }
    }
}

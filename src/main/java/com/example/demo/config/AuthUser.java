package com.example.demo.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/** Principal de Spring Security: añade la versión del token para poder invalidar sesiones. */
public class AuthUser extends User {

    private final int tokenVersion;

    public AuthUser(String username, String password, Collection<? extends GrantedAuthority> authorities, int tokenVersion) {
        super(username, password, authorities);
        this.tokenVersion = tokenVersion;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }
}

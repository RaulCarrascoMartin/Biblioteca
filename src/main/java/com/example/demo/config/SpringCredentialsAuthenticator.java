package com.example.demo.config;

import com.example.demo.shared.CredentialsAuthenticator;
import com.example.demo.shared.InvalidCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class SpringCredentialsAuthenticator implements CredentialsAuthenticator {

    private final AuthenticationManager authenticationManager;

    public SpringCredentialsAuthenticator(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
    }
}

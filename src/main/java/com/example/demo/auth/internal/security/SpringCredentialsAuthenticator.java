package com.example.demo.auth.internal.security;

import com.example.demo.auth.internal.domain.exception.InvalidCredentialsException;
import com.example.demo.auth.internal.domain.CredentialsAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringCredentialsAuthenticator implements CredentialsAuthenticator {

    private final AuthenticationManager authenticationManager;

    @Override
    public void autenticar(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
    }
}

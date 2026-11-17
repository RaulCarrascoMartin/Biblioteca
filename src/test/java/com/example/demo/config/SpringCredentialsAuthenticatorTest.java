package com.example.demo.config;

import com.example.demo.shared.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringCredentialsAuthenticatorTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    void validCredentialsPassSilently() {
        SpringCredentialsAuthenticator authenticator = new SpringCredentialsAuthenticator(authenticationManager);

        assertDoesNotThrow(() -> authenticator.authenticate("a@test.es", "Clave1234"));

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("a@test.es", "Clave1234"));
    }

    @Test
    void anyAuthenticationFailureBecomesInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("x"));
        SpringCredentialsAuthenticator authenticator = new SpringCredentialsAuthenticator(authenticationManager);

        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate("a@test.es", "mala"));
    }
}

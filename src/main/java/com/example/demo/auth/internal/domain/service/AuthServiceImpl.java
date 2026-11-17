package com.example.demo.auth.internal.domain.service;

import com.example.demo.shared.CredentialsAuthenticator;
import com.example.demo.shared.TokenProvider;
import com.example.demo.shared.TokenRevocation;
import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsersModule usersModule;
    private final CredentialsAuthenticator credentialsAuthenticator;
    private final TokenProvider tokenProvider;
    private final TokenRevocation tokenRevocation;

    public AuthServiceImpl(UsersModule usersModule,
                           CredentialsAuthenticator credentialsAuthenticator,
                           TokenProvider tokenProvider,
                           TokenRevocation tokenRevocation) {
        this.usersModule = usersModule;
        this.credentialsAuthenticator = credentialsAuthenticator;
        this.tokenProvider = tokenProvider;
        this.tokenRevocation = tokenRevocation;
    }

    @Override
    public Optional<UserResponseDto> register(UserDto dto) {
        return usersModule.register(dto);
    }

    @Override
    public String login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        credentialsAuthenticator.authenticate(normalizedEmail, password);

        int version = usersModule.getCredentials(normalizedEmail)
                .map(UserCredentialsDto::tokenVersion).orElse(0);
        return tokenProvider.generate(normalizedEmail, version);
    }

    @Override
    public void revokeToken(String token) {
        tokenRevocation.revoke(token);
    }
}

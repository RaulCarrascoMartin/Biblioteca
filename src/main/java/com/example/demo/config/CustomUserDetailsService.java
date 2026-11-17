package com.example.demo.config;

import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserCredentialsDto;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/** Carga las credenciales desde la API pública de {@code users}. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersModule usersModule;

    public CustomUserDetailsService(UsersModule usersModule) {
        this.usersModule = usersModule;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentialsDto user = usersModule.getCredentials(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));

        return new AuthUser(
                user.email(),
                user.password(),
                List.of(new SimpleGrantedAuthority(RequesterArgumentResolver.ROLE_PREFIX + user.role())),
                user.tokenVersion()
        );
    }
}

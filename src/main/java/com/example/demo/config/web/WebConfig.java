package com.example.demo.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** Registra el resolver que inyecta el {@code Requester} en los controllers. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RequesterArgumentResolver requesterArgumentResolver;

    public WebConfig(RequesterArgumentResolver requesterArgumentResolver) {
        this.requesterArgumentResolver = requesterArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(requesterArgumentResolver);
    }
}

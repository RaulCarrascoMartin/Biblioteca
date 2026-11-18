package com.example.demo.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter();

    private MockHttpServletResponse call(String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void loginIsLimitedToFivePerMinutePerIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertEquals(200, call("/api/auth/login", "1.1.1.1").getStatus());
        }
        MockHttpServletResponse blocked = call("/api/auth/login", "1.1.1.1");
        assertEquals(429, blocked.getStatus());
        assertNotNull(blocked.getHeader("Retry-After"));
        assertEquals(200, call("/api/auth/login", "2.2.2.2").getStatus());
    }

    @Test
    void registerIsLimitedToThree() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertEquals(200, call("/api/auth/register", "1.1.1.1").getStatus());
        }
        assertEquals(429, call("/api/auth/register", "1.1.1.1").getStatus());
    }

    @Test
    void otherPathsAreNotLimited() throws Exception {
        for (int i = 0; i < 20; i++) {
            assertEquals(200, call("/api/books", "1.1.1.1").getStatus());
        }
    }

    @Test
    void resetClearsTheCounters() throws Exception {
        for (int i = 0; i < 6; i++) {
            call("/api/auth/login", "1.1.1.1");
        }
        filter.reset();
        assertEquals(200, call("/api/auth/login", "1.1.1.1").getStatus());
        assertNull(call("/api/auth/login", "1.1.1.1").getHeader("Retry-After"));
    }
}

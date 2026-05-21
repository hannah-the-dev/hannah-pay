package com.hannahpay.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void storesAuthenticatedPrincipalInSecurityContext() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtService.extractUserId("token-value")).thenReturn(123L);
        when(jwtService.extractEmail("token-value")).thenReturn("test@example.com");

        filter.doFilter(request, response, filterChain);

        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        assertThat(principal).isInstanceOf(JwtPrincipal.class);
        assertThat(((JwtPrincipal) principal).userId()).isEqualTo(123L);
        assertThat(((JwtPrincipal) principal).email()).isEqualTo("test@example.com");
    }
}

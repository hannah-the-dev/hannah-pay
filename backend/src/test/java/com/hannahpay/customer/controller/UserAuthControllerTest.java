package com.hannahpay.customer.controller;

import com.hannahpay.common.security.JwtAuthenticationFilter;
import com.hannahpay.common.security.JwtPrincipal;
import com.hannahpay.customer.dto.AuthResponse;
import com.hannahpay.customer.dto.UserResponse;
import com.hannahpay.customer.service.UserAuthService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAuthController userAuthController;

    @MockBean
    private UserAuthService userAuthService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void signupReturnsCreated() throws Exception {
        when(userAuthService.signup(any())).thenReturn(
            new AuthResponse(new UserResponse(null, "test@example.com", "Test User", null, null, null, null), "token")
        );

        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\",\"fullName\":\"Test User\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void loginReturnsOk() throws Exception {
        when(userAuthService.login(any())).thenReturn(
            new AuthResponse(new UserResponse(null, "test@example.com", "Test User", null, null, null, null), "token")
        );

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void meReturnsCurrentUser() {
        when(userAuthService.getCurrentUser(123L)).thenReturn(
            new UserResponse(123L, "test@example.com", "Test User", null, null, null, null)
        );

        var response = userAuthController.me(new JwtPrincipal(123L, "test@example.com"));

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().user().email()).isEqualTo("test@example.com");
    }
}

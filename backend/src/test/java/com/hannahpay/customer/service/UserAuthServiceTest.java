package com.hannahpay.customer.service;

import com.hannahpay.common.exception.DuplicateUserException;
import com.hannahpay.common.exception.InvalidCredentialsException;
import com.hannahpay.common.security.JwtProperties;
import com.hannahpay.common.security.JwtService;
import com.hannahpay.customer.domain.User;
import com.hannahpay.customer.dto.LoginRequest;
import com.hannahpay.customer.dto.SignupRequest;
import com.hannahpay.customer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;

    private UserAuthService userAuthService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService(new JwtProperties("0123456789abcdef0123456789abcdef", 3600));
        userAuthService = new UserAuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void signupHashesPasswordAndReturnsUser() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userAuthService.signup(new SignupRequest("Test@Example.com", "password123", "Test User"));

        assertThat(response.user().email()).isEqualTo("test@example.com");
        assertThat(response.user().fullName()).isEqualTo("Test User");
        assertThat(response.accessToken()).isNotBlank();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signupRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userAuthService.signup(new SignupRequest("Test@Example.com", "password123", "Test User")))
            .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User("test@example.com", passwordEncoder.encode("password123"), "Test User");
        user = new User("test@example.com", passwordEncoder.encode("password123"), "Test User");
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userAuthService.login(new LoginRequest("test@example.com", "wrong-password")))
            .isInstanceOf(InvalidCredentialsException.class);
    }
}

package com.hannahpay.customer.service;

import com.hannahpay.common.exception.DuplicateUserException;
import com.hannahpay.common.exception.InvalidCredentialsException;
import com.hannahpay.common.security.JwtService;
import com.hannahpay.customer.domain.User;
import com.hannahpay.customer.dto.AuthResponse;
import com.hannahpay.customer.dto.LoginRequest;
import com.hannahpay.customer.dto.SignupRequest;
import com.hannahpay.customer.dto.UserResponse;
import com.hannahpay.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateUserException("Email already exists");
        }

        User user = new User(normalizedEmail, passwordEncoder.encode(request.password()), request.fullName());
        User saved = userRepository.save(user);
        return new AuthResponse(UserResponse.from(saved), jwtService.generateToken(saved.getId(), saved.getEmail()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new AuthResponse(UserResponse.from(user), jwtService.generateToken(user.getId(), user.getEmail()));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid user"));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid user"));
        user.withdraw();
        return UserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}

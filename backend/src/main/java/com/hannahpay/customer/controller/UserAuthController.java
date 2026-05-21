package com.hannahpay.customer.controller;

import com.hannahpay.customer.dto.AuthResponse;
import com.hannahpay.customer.dto.CurrentUserResponse;
import com.hannahpay.customer.dto.LoginRequest;
import com.hannahpay.customer.dto.SignupRequest;
import com.hannahpay.customer.dto.UserResponse;
import com.hannahpay.common.security.JwtPrincipal;
import com.hannahpay.customer.service.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAuthService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userAuthService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(CurrentUserResponse.from(userAuthService.getCurrentUser(principal.userId())));
    }
}

package com.hannahpay.customer.dto;

public record AuthResponse(UserResponse user, String accessToken) {}

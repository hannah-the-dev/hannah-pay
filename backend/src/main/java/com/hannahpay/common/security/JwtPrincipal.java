package com.hannahpay.common.security;

public record JwtPrincipal(Long userId, String email) {}

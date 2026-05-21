package com.hannahpay.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.jwt")
public record JwtProperties(
    String secret,
    long accessTokenTtlSeconds
) {}

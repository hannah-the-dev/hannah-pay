package com.hannahpay.customer.dto;

public record CurrentUserResponse(UserResponse user) {
    public static CurrentUserResponse from(UserResponse user) {
        return new CurrentUserResponse(user);
    }
}

package com.hannahpay.customer.dto;

import com.hannahpay.customer.domain.User;
import com.hannahpay.customer.domain.UserStatus;

import java.time.OffsetDateTime;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    UserStatus status,
    OffsetDateTime emailVerifiedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getStatus(),
            user.getEmailVerifiedAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

package com.hannahpay.customer.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void normalizesEmailAndTracksLifecycleState() {
        User user = new User("  Test@Example.com  ", "hash", "Hannah Pay");

        user.markEmailVerified(OffsetDateTime.parse("2026-05-12T10:15:30+09:00"));
        user.suspend();

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
        assertThat(user.getFullName()).isEqualTo("Hannah Pay");
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(user.getEmailVerifiedAt()).isEqualTo(OffsetDateTime.parse("2026-05-12T10:15:30+09:00"));
    }

    @Test
    void converterMapsCodesSafely() {
        UserStatusConverter converter = new UserStatusConverter();

        assertThat(converter.convertToDatabaseColumn(UserStatus.ACTIVE)).isEqualTo("A");
        assertThat(converter.convertToEntityAttribute("W")).isEqualTo(UserStatus.WITHDRAWN);
    }
}

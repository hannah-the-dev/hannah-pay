package com.hannahpay.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Convert(converter = UserStatusConverter.class)
    @Column(nullable = false, length = 1)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public User(String email, String passwordHash, String fullName) {
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.status = UserStatus.ACTIVE;
    }

    public void updateProfile(String fullName) {
        this.fullName = fullName;
    }

    public void markEmailVerified(OffsetDateTime verifiedAt) {
        this.emailVerifiedAt = verifiedAt;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void withdraw(OffsetDateTime withdrawnAt) {
        this.status = UserStatus.WITHDRAWN;
        this.deletedAt = withdrawnAt;
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.email = normalizeEmail(this.email);
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
        this.email = normalizeEmail(this.email);
    }
}

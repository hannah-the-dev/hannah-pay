package com.hannahpay.customer.domain;

public enum UserStatus {
    ACTIVE("A"),
    SUSPENDED("S"),
    WITHDRAWN("W");

    private final String code;

    UserStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unsupported user status code: " + code);
    }
}

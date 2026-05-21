package com.hannahpay.common.exception;

public class DuplicateUserException extends BusinessException {

    public DuplicateUserException(String message) {
        super(message);
    }
}

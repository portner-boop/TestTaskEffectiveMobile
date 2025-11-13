package com.example.testtaskeffectivemobile.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND("USER_NOT_FOUND","User not ofund with id %s", HttpStatus.NOT_FOUND),
    CHANGE_PASSWORD_MISMATCH("CHANGE_PASSWORD_MISMATCH", "Current password and new password are not the same", HttpStatus.BAD_REQUEST),
    INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "Current password is invalid", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_DEACTIVATED("ACCOUNT_ALREADY_DEACTIVATED", "Account already deactivated", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_ACTIVATED("ACCOUNT_ALREADY_ACTIVATED", "Account already activated", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "Current password and new password are not the same", HttpStatus.BAD_REQUEST),
    ERR_USER_DISABLED("ERR_USER_DISABLED", "User disabled", HttpStatus.UNAUTHORIZED),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Username and / or password is incorrect", HttpStatus.UNAUTHORIZED),
    USERNAME_NOT_FOUND("USERNAME_NOT_FOUND", "Username not found", HttpStatus.NOT_FOUND),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPRION", "Internal error", HttpStatus.INTERNAL_SERVER_ERROR),
    CARD_NOT_FOUND("CARD_NOT_FOUND", "Card not found with id %s", HttpStatus.NOT_FOUND),
    CARD_ALREADY_BLOCKED("CARD_ALREADY_BLOCKED", "Card already blocked", HttpStatus.BAD_REQUEST),
    CARD_ALREADY_ACTIVE("CARD_ALREADY_ACTIVE", "Card already active", HttpStatus.BAD_REQUEST),
    CARD_NOT_ACTIVE("CARD_NOT_ACTIVE", "Card is not active", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("ACCESS_DENIED", "Access denied", HttpStatus.FORBIDDEN),
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", "Insufficient funds", HttpStatus.BAD_REQUEST),
    USER_ACCOUNT_DISABLED("USER_ACCOUNT_DISABLED", "User account is disabled", HttpStatus.FORBIDDEN),
    USER_ACCOUNT_LOCKED("USER_ACCOUNT_LOCKED", "User account is locked", HttpStatus.FORBIDDEN),
    USER_DATA_INCOMPLETE("USER_DATA_INCOMPLETE", "User data is incomplete: %s", HttpStatus.BAD_REQUEST),
    USER_DATA_INVALID("USER_DATA_INVALID", "User data is invalid: %s", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT("INVALID_AMOUNT", "Invalid amount", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(final String code,
              final String defaultMessage,
              final HttpStatus status){
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }
}

package com.ss.exception.http;

import com.ss.exception.ResponseError;
import lombok.Getter;

@Getter
public enum InvalidInputError implements ResponseError {

    PERMISSION_GROUP_INVALID(4010000, "permission group is not existed"),
    USER_PASSWORD_INVALID(4010001, "Invalid username/password supplied"),
    TOKEN_EXPIRED(4010001, "Token was expired. Please make a new signin request"),

    ;

    private Integer code;
    private String message;

    InvalidInputError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return 400;
    }

    public Integer getCode() {
        return code;
    }

    public String getCodeString() {
        return code.toString();
    }
}

package com.ss.exception.http;

import com.ss.exception.ResponseError;
import lombok.Getter;

@Getter
public enum DuplicatedError implements ResponseError {
    USERNAME_DUPLICATED(4090001, "username is duplicated"),
    USER_CODE_DUPLICATED(4090002, "user code is duplicated"),
    PRODUCT_CODE_DUPLICATED(4090100, "product code is duplicated"),
    PRODUCT_NUMBER_DUPLICATED(4090101, "product number is duplicated"),
    ;

    private Integer code;
    private String message;

    DuplicatedError(Integer code, String message) {
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
        return 409;
    }

    public Integer getCode() {
        return code;
    }
}

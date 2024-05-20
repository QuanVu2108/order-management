package com.ss.exception.http;

import com.ss.exception.ResponseError;
import lombok.Getter;

@Getter
public enum NotFoundError implements ResponseError {
    USER_NOT_FOUND(4040000, "user is not existed" ),
    PERMISSION_GROUP_NOT_FOUND(4040001, "permission group is not existed" ),
    PRODUCT_NOT_FOUND(4040100, "product not found" ),
    ;

    private Integer code;
    private String message;

    NotFoundError(Integer code, String message) {
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
        return 404;
    }

    public Integer getCode() {
        return code;
    }
}

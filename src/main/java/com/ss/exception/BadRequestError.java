package com.ss.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum BadRequestError implements ResponseError {

    INVALID_INPUT(4000000, "Invalid input : {0}"),
    IMPORT_FAILED(4000001, "Import failed!!! : {0}"),

    CONNECT_CLIENT_FAILED(4000002, "Cannot connect to client: {0}"),
    AUTHENTICATE_FAILED(4010000, "invalid credentials!!!"),
    ;

    private final int errorCode;
    private final String message;

    BadRequestError(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public int getStatus() {
        return HttpStatus.BAD_REQUEST.value();
    }
}

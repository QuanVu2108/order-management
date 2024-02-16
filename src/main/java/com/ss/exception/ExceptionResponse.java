package com.ss.exception;

import lombok.Getter;
import lombok.Setter;

import static com.ss.exception.BadRequestError.INVALID_INPUT;

@Getter
@Setter
public class ExceptionResponse extends RuntimeException {
    private ResponseError error;
    private Object[] params;
    private Object data;

    public ExceptionResponse(Object data, String message, ResponseError error, Object... params) {
        this(data, message, null, error, params);
    }

    public ExceptionResponse(String message, Throwable cause, ResponseError error) {
        this(null, message, cause, error);
    }

    public ExceptionResponse(String message) {
        this(null, message, null, INVALID_INPUT, null);
    }

    public ExceptionResponse(String message, ResponseError error, Object... params) {
        this(null, message, null, error, params);
    }


    public ExceptionResponse(Object data, String message, Throwable cause, ResponseError error, Object... params) {
        super(message, cause);
        this.error = error;
        this.params = params == null ? new Object[]{} : params;
        this.data = data;
    }
}

package com.ss.exception;

import com.ss.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
@Order
public class ExceptionHandleAdvice {

    @ExceptionHandler(ExceptionResponse.class)
    public ResponseEntity<ErrorResponse<Object>> handleResponseException(ExceptionResponse e, HttpServletRequest request) {
        log.warn("Failed to handle request {}: {}", request.getRequestURI(), e.getError().getMessage(), e);
        ResponseError error = e.getError();
        return ResponseEntity.status(error.getStatus())
                .body(ErrorResponse.<Object>builder()
                        .code(((BadRequestError) e.getError()).getErrorCode())
                        .error(error.getName())
                        .message(e.getMessage())
                        .data(e.getData())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Failed to handle request {}: {}", request.getRequestURI(), e.getMessage(), e);
        BadRequestError error = BadRequestError.INVALID_INPUT;
        return ResponseEntity.status(error.getStatus())
                .body(ErrorResponse.<Object>builder()
                        .code(error.getErrorCode())
                        .error(error.getName())
                        .message(e.getMessage())
                        .build());
    }

}

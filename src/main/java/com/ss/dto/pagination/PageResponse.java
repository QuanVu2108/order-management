package com.ss.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private int code;
    private Long total;
    private String message;
    private Paging paging;
    private List<T> data;

    public static <T> PageResponse<T> succeed(HttpStatus status, PageResponse data) {
        data.setCode(status.value());
        return data;
    }

    public static <T> PageResponse<T> succeed(HttpStatus status, String message, PageResponse data) {
        data.setCode(status.value());
        data.setMessage(message);
        return data;
    }
}

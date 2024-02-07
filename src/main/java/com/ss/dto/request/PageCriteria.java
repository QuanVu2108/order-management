package com.ss.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class PageCriteria {
    @Min(value = 1, message = "page must be greater than 0")
    private Integer page = 1;

    @Min(value = 1, message = "limit must be greater than 0")
    private Integer limit = 1000000;
}

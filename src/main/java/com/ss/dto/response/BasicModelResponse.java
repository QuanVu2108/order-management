package com.ss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BasicModelResponse {
    private UUID id;
    private String code;
    private String name;
    private Long time;
}

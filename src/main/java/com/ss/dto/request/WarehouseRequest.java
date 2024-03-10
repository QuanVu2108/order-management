package com.ss.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WarehouseRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String phoneNumber;
    private String address;
    private String description;
}

package com.ss.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PermissionGroup {
    @NotBlank
    private String name;
    private String phoneNumber;
    private String address;
    private String description;
}

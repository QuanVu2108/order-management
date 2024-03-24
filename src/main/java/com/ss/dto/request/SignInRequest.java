package com.ss.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SignInRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
}

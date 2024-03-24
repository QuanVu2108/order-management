package com.ss.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class PermissionMenuRequest {
    @NotBlank
    private String name;

}

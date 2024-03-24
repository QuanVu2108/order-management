package com.ss.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class PermissionGroupRequest {
    @NotBlank
    private String groupName;

    private List<PermissionRequest> permissions;
}

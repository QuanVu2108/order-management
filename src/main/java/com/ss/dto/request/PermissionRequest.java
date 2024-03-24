package com.ss.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class PermissionRequest {
    @NotNull
    private UUID permissionMenuId;

    private Boolean isViewed;

    private Boolean isAdded;

    private Boolean isEdited;

    private Boolean isDeleted;

    private Boolean isConfirmed;

    public PermissionRequest () {
        this.isViewed = false;
        this.isAdded = false;
        this.isEdited = false;
        this.isDeleted = false;
        this.isConfirmed = false;
    }
}

package com.ss.dto.response;

import com.ss.model.PermissionGroupModel;
import com.ss.model.PermissionModel;
import lombok.Data;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@Data
@ToString
public class PermissionGroupResponse {
    private UUID id;

    private String groupName;

    private Set<PermissionModel> permissions;

    public PermissionGroupResponse(PermissionGroupModel permissionGroup, Set<PermissionModel> permissions) {
        this.id = permissionGroup.getId();
        this.groupName = permissionGroup.getName();
        this.permissions = permissions;
    }
}

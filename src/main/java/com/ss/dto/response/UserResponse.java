package com.ss.dto.response;

import com.ss.model.PermissionGroupModel;
import com.ss.model.StoreModel;
import com.ss.model.UserModel;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;

    private String userName;

    private String fullName;

    private PermissionGroupModel permissionGroup;

    private String position;

    private String email;

    private Set<StoreModel> stores;

    private Boolean isActive;

    public UserResponse(UserModel user, PermissionGroupModel permissionGroup, Set<StoreModel> stores) {
        this.id = user.getId();
        this.userName = user.getUsername();
        this.fullName = user.getFullName();
        this.position = user.getPosition();
        this.email = user.getEmail();
        this.isActive = user.getIsActive();
        this.permissionGroup = permissionGroup;
        this.stores = stores;
    }
}

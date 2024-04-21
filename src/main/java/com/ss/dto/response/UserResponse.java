package com.ss.dto.response;

import com.ss.model.PermissionGroupModel;
import com.ss.model.StoreModel;
import com.ss.model.UserModel;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class UserResponse {
    private UUID id;

    private String userCode;

    private String userName;

    private String fullName;

    private PermissionGroupModel permissionGroup;

    private String position;

    private String email;

    private Set<StoreRes> stores;

    private Boolean isActive;

    public UserResponse(UserModel user, PermissionGroupModel permissionGroup, Set<StoreModel> stores) {
        this.id = user.getId();
        this.userCode = user.getUserCode();
        this.userName = user.getUsername();
        this.fullName = user.getFullName();
        this.position = user.getPosition();
        this.email = user.getEmail();
        this.isActive = user.getIsActive();
        this.permissionGroup = permissionGroup;
        this.stores = stores.stream()
                .map(item -> new StoreRes(item))
                .collect(Collectors.toSet());
    }

    @Data
    private static class StoreRes {
        private UUID id;
        private String name;
        private String address;

        public StoreRes(StoreModel store) {
            this.id = store.getId();
            this.name = store.getName();
            this.address = store.getAddress();
        }
    }
}

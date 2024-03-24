package com.ss.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ss.dto.request.PermissionRequest;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "permission_tbl")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionModel extends AuditModel {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_group_id")
    @JsonBackReference
    private PermissionGroupModel permissionGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_menu_id")
    @JsonBackReference
    private PermissionMenuModel permissionMenu;

    @Column(name = "is_viewed")
    private Boolean isViewed;

    @Column(name = "is_added")
    private Boolean isAdded;

    @Column(name = "is_edited")
    private Boolean isEdited;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;

    public PermissionModel(PermissionGroupModel permissionGroup, PermissionMenuModel permissionMenu) {
        this.id = UUID.randomUUID();
        this.permissionGroup = permissionGroup;
        this.permissionMenu = permissionMenu;
        this.isViewed = false;
        this.isAdded = false;
        this.isEdited = false;
        this.isDeleted = false;
        this.isConfirmed = false;
    }

    public PermissionModel(PermissionRequest permissionRequest, PermissionGroupModel permissionGroup, PermissionMenuModel permissionMenu) {
        this.id = UUID.randomUUID();
        this.permissionGroup = permissionGroup;
        this.permissionMenu = permissionMenu;
        this.isViewed = permissionRequest.getIsViewed();
        this.isAdded = permissionRequest.getIsAdded();
        this.isEdited = permissionRequest.getIsEdited();
        this.isDeleted = permissionRequest.getIsDeleted();
        this.isConfirmed = permissionRequest.getIsConfirmed();
    }

    public void update(PermissionRequest permissionRequest) {
        if (permissionRequest != null) {
            this.isViewed = permissionRequest.getIsViewed();
            this.isAdded = permissionRequest.getIsAdded();
            this.isEdited = permissionRequest.getIsEdited();
            this.isDeleted = permissionRequest.getIsDeleted();
            this.isConfirmed = permissionRequest.getIsConfirmed();
        }
    }
}

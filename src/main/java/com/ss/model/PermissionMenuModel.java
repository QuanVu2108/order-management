package com.ss.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ss.dto.request.PermissionMenuRequest;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permission_menu_tbl")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@AllArgsConstructor
public class PermissionMenuModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "permissionMenu", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<PermissionModel> permissions;

    public PermissionMenuModel() {
        this.id = UUID.randomUUID();
        this.deleted = false;
    }

    public void update(PermissionMenuRequest request) {
        this.name = request.getName();
    }
}

package com.ss.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permission_group_tbl")
@Where(clause = "deleted = false")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PermissionGroupModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "permissionGroup", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<PermissionModel> permissions;

    public PermissionGroupModel(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.deleted = false;
    }
}

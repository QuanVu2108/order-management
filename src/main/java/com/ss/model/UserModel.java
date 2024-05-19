package com.ss.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ss.dto.request.UserRequest;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Table(name = "user_tbl")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel extends AuditModel {

    @Id
    private UUID id;

    @Column(name = "user_name", unique = true, nullable = false)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "position")
    private String position;

    @Size(min = 8, message = "Minimum password length: 8 characters")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    List<RoleModel> roles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_group_id")
    @JsonBackReference
    private PermissionGroupModel permissionGroup;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_store",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id"))
    private Set<StoreModel> stores = new HashSet<>();

    @Column(name = "is_active")
    private Boolean isActive;

    public UserModel(UserRequest request) {
        this.id = UUID.randomUUID();
        this.roles = Arrays.asList(RoleModel.ROLE_ADMIN);
        this.username = request.getUserName();
        this.deleted = false;
    }

    public void update(UserRequest request, String password, PermissionGroupModel permissionGroupModel, Set<StoreModel> stores) {
        this.userCode = request.getUserCode();
        this.fullName = request.getFullName();
        this.password = password;
        this.position = request.getPosition();
        this.email = request.getEmail();
        this.isActive = request.getIsActive();
        this.permissionGroup = permissionGroupModel;
        this.stores = stores;
    }

}

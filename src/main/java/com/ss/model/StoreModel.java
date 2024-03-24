package com.ss.model;


import com.ss.dto.Store;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "store_tbl")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@AllArgsConstructor
public class StoreModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "text")
    private String address;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ManyToMany(mappedBy = "stores")
    private Set<UserModel> users = new HashSet<>();

    public StoreModel() {
        this.id = UUID.randomUUID();
    }

    public void update(Store request) {
        this.name = request.getName();
        this.phoneNumber = request.getPhoneNumber();
        this.address = request.getAddress();
        this.description = request.getDescription();
    }
}

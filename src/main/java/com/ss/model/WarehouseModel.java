package com.ss.model;


import com.ss.dto.request.WarehouseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "warehouse")
@Where(clause = "deleted = false")
@Data
@Builder
@AllArgsConstructor
public class WarehouseModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "text")
    private String address;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    public WarehouseModel() {
        this.id = UUID.randomUUID();
        setAuditDefault();
    }

    public void update(WarehouseRequest request) {
        this.code = request.getCode();
        this.name = request.getName();
        this.phoneNumber = request.getPhoneNumber();
        this.address = request.getAddress();
        this.description = request.getDescription();
    }
}

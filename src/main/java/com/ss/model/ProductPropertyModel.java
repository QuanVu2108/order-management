package com.ss.model;


import com.ss.dto.request.ProductPropertyRequest;
import com.ss.enums.ProductPropertyType;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_property_tbl")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPropertyModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ProductPropertyType type;

    public ProductPropertyModel(String code, ProductPropertyType type) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.type = type;
    }

    public void update(ProductPropertyRequest request) {
        this.name = request.getName();
    }
}

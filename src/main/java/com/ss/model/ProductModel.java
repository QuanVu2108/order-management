package com.ss.model;


import com.ss.dto.request.ProductRequest;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Table(name = "product_tbl")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category", referencedColumnName = "id")
    private ProductPropertyModel category;

    @ManyToOne
    @JoinColumn(name = "brand", referencedColumnName = "id")
    private ProductPropertyModel brand;

    @Column(name = "sold_price")
    private Long soldPrice;

    @Column(name = "cost_price")
    private Long costPrice;

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private String size;

    @Column(name = "is_active")
    private Boolean isActive;

    public void update(ProductRequest request, ProductPropertyModel category, ProductPropertyModel brand) {
        this.code = request.getCode();
        this.name = request.getName();
        this.costPrice = request.getCostPrice();
        this.soldPrice = request.getSoldPrice();
        this.color = request.getColor();
        this.size = request.getSize();
        this.category = category;
        this.brand = brand;
    }
}

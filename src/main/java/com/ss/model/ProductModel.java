package com.ss.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ss.dto.request.ProductRequest;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Set;

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

    @Column(name = "product_number")
    private String productNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category", referencedColumnName = "id")
    private ProductPropertyModel category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand", referencedColumnName = "id")
    private ProductPropertyModel brand;

    @Column(name = "sold_price")
    private Long soldPrice;

    @Column(name = "cost_price")
    private Long costPrice;

    @Column(name = "incentive")
    private Long incentive;

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private String size;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FileModel> images;

    @Column(name = "qr_code")
    private byte[] qrCode;

    public void update(ProductRequest request, ProductPropertyModel category, ProductPropertyModel brand) {
        this.code = request.getCode();
        this.productNumber = request.getProductNumber();
        this.name = request.getName();
        this.costPrice = request.getCostPrice();
        this.soldPrice = request.getSoldPrice();
        this.color = request.getColor();
        this.size = request.getSize();
        this.category = category;
        this.brand = brand;
    }

    public String getInfo() {
        StringBuilder info = new StringBuilder();
        info.append(this.name);
        info.append("(").append(this.productNumber).append(")");
        return info.toString();
    }
}

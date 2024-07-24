
package com.ss.dto.response;

import com.ss.model.ProductModel;
import com.ss.model.ProductPropertyModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Set;

@Data
@NoArgsConstructor
public class ProductResponse extends AuditResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String code;

    private String productNumber;

    private String name;

    private String description;

    private ProductPropertyModel category;

    private ProductPropertyModel brand;

    private Long soldPrice;

    private Long costPrice;

    private Long incentive;

    private String color;

    private String size;

    private Boolean isActive;

    private Set<FileResponse> images;

    private byte[] qrCode;

    public ProductResponse(ProductModel model) {
        this.id = model.getId();
        this.code = model.getCode();
        this.productNumber = model.getProductNumber();
        this.name = model.getName();
        this.description = model.getDescription();
        this.soldPrice = model.getSoldPrice();
        this.costPrice = model.getCostPrice();
        this.incentive = model.getIncentive();
        this.color = model.getColor();
        this.size = model.getSize();
        this.isActive = model.getIsActive();
        this.qrCode = model.getQrCode();
    }
}

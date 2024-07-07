
package com.ss.dto.response;

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

}

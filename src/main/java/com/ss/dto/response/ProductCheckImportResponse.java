
package com.ss.dto.response;

import com.ss.model.ProductModel;
import com.ss.model.StoreModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductCheckImportResponse {
    private int idx;
    private String code;
    private String number;
    private String storeName;
    private Long quantity;
    private Double cost;
    private Double incentive;
    private ProductModel product;
    private StoreModel store;

    public ProductCheckImportResponse(int idx, String code, String number, String storeName, Long quantity, Double cost, Double incentive) {
        this.idx = idx;
        this.code = code;
        this.number = number;
        this.storeName = storeName;
        this.quantity = quantity;
        this.cost = cost;
        this.incentive = incentive;
    }
}

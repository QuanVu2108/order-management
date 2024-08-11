
package com.ss.dto.response;

import com.ss.model.StoreModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ProductCheckImportResponse {
    private int idx;
    private UUID orderItemId;
    private String code;
    private String number;
    private String storeName;
    private Long quantity;
    private Double cost;
    private Double incentive;
    private ProductResponse product;
    private StoreResponse store;

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

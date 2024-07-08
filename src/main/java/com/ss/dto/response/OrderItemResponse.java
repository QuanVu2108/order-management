
package com.ss.dto.response;

import com.ss.enums.OrderItemStatus;
import com.ss.model.StoreModel;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID id;

    private String code;

    private ProductResponse product;

    private String note;

    private Long quantityOrder;

    private Long quantityReality;

    private Long quantityReceived;

    private Long quantitySent;

    private Long quantityInCart;

    private Long quantityChecked;

    private Double cost;

    private Double costReality;

    private Double costTotal;

    private StoreModel store;

    private Long delayDay;

    private OrderItemStatus status;

    private BasicModelResponse order;

    private Instant updatedAt;
}

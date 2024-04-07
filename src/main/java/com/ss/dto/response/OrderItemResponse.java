
package com.ss.dto.response;

import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderModel;
import com.ss.model.ProductModel;
import com.ss.model.StoreModel;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID id;

    private String code;

    private ProductModel product;

    private String note;

    private Long quantityOrder;

    private Long quantityReality;

    private Long quantityReceived;

    private Double cost;

    private Double costReality;

    private Double costTotal;

    private StoreModel store;

    private Long delayDay;

    private OrderItemStatus status;

    private OrderResponse order;
}

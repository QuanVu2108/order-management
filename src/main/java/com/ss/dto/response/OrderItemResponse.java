
package com.ss.dto.response;

import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.StoreModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    private Double incentive;

    private StoreModel store;

    private Long delayDay;

    private OrderItemStatus status;

    private BasicModelResponse order;

    private Instant updatedAt;

    public OrderItemResponse(OrderItemModel model) {
        this.id = model.getId();
        this.code = model.getCode();
        this.note = model.getNote();
        this.quantityOrder = model.getQuantityOrder();
        this.quantityReality = model.getQuantityReality();
        this.quantityReceived = model.getQuantityReceived();
        this.quantitySent = model.getQuantitySent();
        this.quantityInCart = model.getQuantityInCart();
        this.quantityChecked = model.getQuantityChecked();
        this.cost = model.getCost();
        this.costReality = model.getCostReality();
        this.costTotal = model.getCostTotal();
        this.incentive = model.getIncentive();
        this.delayDay = model.getDelayDay();
        this.status = model.getStatus();
        this.updatedAt = model.getUpdatedAt();
    }
}

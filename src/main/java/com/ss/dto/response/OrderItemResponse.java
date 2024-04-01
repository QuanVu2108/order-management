package com.ss.dto.response;

import com.ss.enums.OrderItemStatus;
import com.ss.model.FileModel;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.model.StoreModel;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderItemResponse {

    private UUID id;

    private String name;

    private String content;

    private Long quantityOrder;

    private Long quantityReality;

    private Double priceOrder;

    private Double priceReality;

    private StoreModel store;

    private OrderItemStatus status;

    private OrderModel orderModel;

    public OrderItemResponse(OrderItemModel model, StoreModel store) {
        this.id = model.getId();
        this.name = model.getName();
        this.content = model.getContent();
        this.quantityOrder = model.getQuantityOrder();
        this.quantityReality = model.getQuantityReality();
        this.priceOrder = model.getPriceOrder();
        this.priceReality = model.getPriceReality();
        this.status = model.getStatus();
        this.orderModel = model.getOrderModel();
        this.store = store;
    }

}

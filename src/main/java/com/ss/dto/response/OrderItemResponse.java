package com.ss.dto.response;

import com.ss.enums.OrderItemStatus;
import com.ss.model.FileModel;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.model.WarehouseModel;
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

    private WarehouseModel warehouse;

    private OrderItemStatus status;

    private List<FileModel> files;

    private OrderModel orderModel;

    public OrderItemResponse(OrderItemModel model, WarehouseModel warehouse) {
        this.id = model.getId();
        this.name = model.getName();
        this.content = model.getContent();
        this.quantityOrder = model.getQuantityOrder();
        this.quantityReality = model.getQuantityReality();
        this.priceOrder = model.getPriceOrder();
        this.priceReality = model.getPriceReality();
        this.status = model.getStatus();
        this.files = model.getFiles();
        this.orderModel = model.getOrderModel();
        this.warehouse = warehouse;
    }

}

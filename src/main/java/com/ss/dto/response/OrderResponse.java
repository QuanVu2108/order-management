package com.ss.dto.response;

import com.ss.enums.OrderStatus;
import com.ss.model.OrderModel;
import com.ss.model.UserModel;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;

    private String code;

    private Long date;

    private Long totalQuantity;

    private Double totalCost;

    private Double actualCost;

    private Double incentive;

    private String note;

    private Long receivedQuantity;

    private OrderStatus status;

    private BasicModelResponse createdBy;

    public OrderResponse(OrderModel order, UserModel user) {
        this.id = order.getId();
        this.code = order.getCode();
        this.date = order.getDate();
        this.totalQuantity = order.getTotalQuantity();
        this.totalCost = order.getTotalCost();
        this.actualCost = order.getActualCost();
        this.incentive = order.getIncentive();
        this.note = order.getNote();
        this.receivedQuantity = order.getReceivedQuantity();
        this.status = order.getStatus();
        if (user != null)
            this.createdBy = new BasicModelResponse(user.getId(), user.getUsername(), user.getFullName(), null);
    }
}

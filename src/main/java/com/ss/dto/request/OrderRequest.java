package com.ss.dto.request;

import com.ss.enums.OrderStatus;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class OrderRequest {

    private Long totalQuantity;

    private Double totalCost;

    private Double actualCost;

    private Double incentive;

    @NotEmpty
    private List<OrderItemRequest> items;

    private OrderStatus status;
}

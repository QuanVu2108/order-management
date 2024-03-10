package com.ss.dto.request;

import com.ss.enums.OrderItemStatus;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
public class OrderItemRequest {

    private UUID orderId;

    private String name;

    private String content;

    private Long quantityOrder;

    private Long quantityReality;

    private Double priceOrder;

    private Double priceReality;

    private UUID warehouseId;

    @NotNull
    private OrderItemStatus status;
}

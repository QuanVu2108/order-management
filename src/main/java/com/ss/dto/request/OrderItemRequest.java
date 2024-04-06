package com.ss.dto.request;

import com.ss.enums.OrderItemStatus;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
public class OrderItemRequest {

    private UUID id;

    @NotNull
    private Long productId;

    @NotNull
    private Long quantity;

    private Long receivedQuantity;

    @NotNull
    private Double cost;

    @NotNull
    private Double costTotal;

    @NotNull
    private UUID storeId;

    @NotNull
    private OrderItemStatus status;

    private String note;
}

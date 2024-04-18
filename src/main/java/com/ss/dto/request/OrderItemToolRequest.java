package com.ss.dto.request;

import com.ss.enums.OrderItemStatus;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
public class OrderItemToolRequest {

    private UUID id;

    private Long delayDay;

    private Long quantityReality;

    private Double costReality;

    private Long quantitySent;

    private Long quantityInCart;

    private Long quantityChecked;

    @NotNull
    private OrderItemStatus status;

    private String note;
}

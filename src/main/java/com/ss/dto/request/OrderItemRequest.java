package com.ss.dto.request;

import lombok.Builder;
import lombok.Data;

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
}

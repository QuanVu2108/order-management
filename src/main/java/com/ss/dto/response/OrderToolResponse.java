package com.ss.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderToolResponse {
    private OrderResponse order;
    private OrderItemStatisticResponse statistic;
}

package com.ss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderToolResponse {
    private List<OrderResponse> orders;
    private OrderStatisticResponse statistic;
}

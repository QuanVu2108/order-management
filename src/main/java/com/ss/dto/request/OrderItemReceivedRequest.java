package com.ss.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class OrderItemReceivedRequest {

    @NotNull
    private Long receivedQuantity;

    private String note;
}

package com.ss.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
public class OrderItemReceivedMultiRequest extends OrderItemReceivedRequest {

    @NotNull
    private UUID id;

    OrderItemReceivedMultiRequest(@NotNull Long receivedQuantity, String note) {
        super(receivedQuantity, note);
    }
}

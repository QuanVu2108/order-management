package com.ss.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
public class OrderItemUpdatedDetailRequest {

    @NotNull
    private UUID id;

    @NotNull
    private Long quantity;

    @NotNull
    private Double cost;
}

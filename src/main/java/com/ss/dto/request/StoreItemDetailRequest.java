package com.ss.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class StoreItemDetailRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Long quantity;

    private Double cost;

    private String note;

}

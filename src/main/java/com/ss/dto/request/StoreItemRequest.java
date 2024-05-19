package com.ss.dto.request;

import com.ss.enums.StoreItemType;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StoreItemRequest {

    @NotNull
    private UUID storeId;

    @NotEmpty
    private List<StoreItemDetailRequest> items;

    @NotNull
    private StoreItemType type;

    private UUID targetStore;

    private UUID orderId;
}

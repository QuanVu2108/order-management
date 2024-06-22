package com.ss.repository.query;

import com.ss.enums.StoreItemType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Builder
@Data
public class StoreItemQuery {
    private String product;
    private List<Long> productIds;
    private String store;
    private UUID order;
    private StoreItemType type;
    private Long fromTime;
    private Long toTime;
}

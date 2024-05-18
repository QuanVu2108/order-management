package com.ss.repository.query;

import com.ss.enums.StoreItemType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class StoreItemQuery {
    private String product;
    private String store;
    private UUID order;
    private StoreItemType type;
    private Long fromTime;
    private Long toTime;
}

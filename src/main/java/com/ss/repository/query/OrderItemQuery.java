package com.ss.repository.query;

import com.ss.enums.OrderItemStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Builder
@Data
public class OrderItemQuery {
    private List<UUID> ids;
    private List<UUID> orderIds;
    private String orderCode;
    private String productCode;
    private List<Long> productIds;
    private String store;
    private List<UUID> storeIds;
    private List<OrderItemStatus> statuses;
}

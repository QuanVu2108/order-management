package com.ss.repository.query;

import com.ss.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Builder
@Data
public class OrderQuery {
    private List<UUID> ids;
    private String code;
    private List<OrderStatus> statuses;
    private Long fromDate;
    private Long toDate;
    private List<String> createdUsers;
}

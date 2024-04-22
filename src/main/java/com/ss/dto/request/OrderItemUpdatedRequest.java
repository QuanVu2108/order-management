package com.ss.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderItemUpdatedRequest {

    @NotEmpty
    private List<OrderItemUpdatedDetailRequest> details;

    private boolean isApproved;
}

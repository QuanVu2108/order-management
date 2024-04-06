package com.ss.dto.request;

import com.ss.enums.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemSubmittedRequest {

    @NotEmpty
    private List<UUID> ids;

    @NotNull
    private OrderItemStatus status;
}

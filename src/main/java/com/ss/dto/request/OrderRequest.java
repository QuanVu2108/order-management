package com.ss.dto.request;

import com.ss.model.OrderItemModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
public class OrderRequest {

    private String title;

    private String content;

    @NotEmpty
    private List<OrderItemModel> items;
}

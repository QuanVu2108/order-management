package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.OrderItemSubmittedRequest;
import com.ss.dto.request.OrderItemToolRequest;
import com.ss.dto.response.ServiceResponse;
import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderItemModel;
import com.ss.repository.query.OrderItemQuery;
import com.ss.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tool")
@AllArgsConstructor
public class ToolController {

    private final OrderService orderService;

    @GetMapping("/order-item")
    PageResponse<OrderItemModel> getOrderItem(
            @RequestParam(name = "ids", required = false) List<UUID> ids,
            @RequestParam(name = "orderIds", required = false) List<UUID> orderIds,
            @RequestParam(name = "orderCode", required = false) String orderCode,
            @RequestParam(name = "productIds", required = false) List<Long> productIds,
            @RequestParam(name = "productCode", required = false) String productCode,
            @RequestParam(name = "storeIds", required = false) List<UUID> storeIds,
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "statuses", required = false) List<OrderItemStatus> statuses,
            @Valid PageCriteria pageCriteria) {

        OrderItemQuery orderItemQuery = OrderItemQuery.builder()
                .ids(ids)
                .orderIds(orderIds)
                .orderCode(orderCode)
                .productCode(productCode)
                .productIds(productIds)
                .store(store)
                .storeIds(storeIds)
                .statuses(statuses)
                .build();
        return PageResponse.succeed(HttpStatus.OK, orderService.searchOrderItem(orderItemQuery, pageCriteria));
    }

    @PutMapping("/order-item/{orderItemId}")
    ServiceResponse<OrderItemModel> updateOrderItem(
            @PathVariable @Valid UUID orderItemId,
            @RequestBody @Valid OrderItemToolRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.updateOrderItemByTool(orderItemId, request));
    }

    @PostMapping("/order-item/submit")
    ServiceResponse<List<OrderItemModel>> submitOrderItem(
            @RequestBody @Valid OrderItemSubmittedRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.submitByTool(request));
    }

}

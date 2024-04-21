package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemReceivedMultiRequest;
import com.ss.dto.request.OrderItemReceivedRequest;
import com.ss.dto.request.OrderRequest;
import com.ss.dto.response.*;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.repository.query.OrderItemQuery;
import com.ss.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    ServiceResponse<OrderModel> createOrder(@RequestBody @Valid OrderRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.createOrder(request));
    }

    @PutMapping("/{orderId}")
    ServiceResponse<OrderModel> updateOrder(
            @PathVariable @Valid UUID orderId,
            @RequestBody @Valid OrderRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.updateOrder(orderId, request));
    }

    @GetMapping("/statistic")
    ServiceResponse<OrderStatisticResponse> getStatistic(
            @RequestParam(name = "ids", required = false) List<UUID> ids,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "statuses", required = false) List<OrderStatus> statuses,
            @RequestParam(name = "fromDate", required = false) Long fromDate,
            @RequestParam(name = "toDate", required = false) Long toDate,
            @RequestParam(name = "createdUser", required = false) String createdUser) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.getStatistic(ids, code, statuses, fromDate, toDate, createdUser));
    }

    @GetMapping
    PageResponse<OrderResponse> searchOrder(
            @RequestParam(name = "ids", required = false) List<UUID> ids,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "statuses", required = false) List<OrderStatus> statuses,
            @RequestParam(name = "fromDate", required = false) Long fromDate,
            @RequestParam(name = "toDate", required = false) Long toDate,
            @RequestParam(name = "createdUser", required = false) String createdUser,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, orderService.searchOrder(ids, code, statuses, fromDate, toDate, createdUser, pageCriteria));
    }

    @PutMapping("/receive-items/{orderId}")
    ServiceResponse<Void> receiveItems(
            @PathVariable @Valid UUID orderId,
            @RequestBody @Valid List<OrderItemReceivedMultiRequest> request) {
        orderService.receiveItemMulti(orderId, request);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping("/order-item")
    PageResponse<OrderItemResponse> searchOrderItem(
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

    @PutMapping("/order-item/receive/{orderItemId}")
    ServiceResponse<OrderItemModel> updateOrderItem(
            @PathVariable @Valid UUID orderItemId,
            @RequestBody @Valid OrderItemReceivedRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.receiveItem(orderItemId, request));
    }

    @PutMapping("/order-item/cancel/{orderItemId}")
    ServiceResponse<OrderItemModel> cancelOrderItem(@PathVariable @Valid UUID orderItemId) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.cancelItem(orderItemId));
    }

    @GetMapping("/order-item/statistic")
    ServiceResponse<OrderItemStatisticResponse> getOrderItemStatistic(
            @RequestParam(name = "ids", required = false) List<UUID> ids,
            @RequestParam(name = "orderIds", required = false) List<UUID> orderIds,
            @RequestParam(name = "orderCode", required = false) String orderCode,
            @RequestParam(name = "productIds", required = false) List<Long> productIds,
            @RequestParam(name = "productCode", required = false) String productCode,
            @RequestParam(name = "storeIds", required = false) List<UUID> storeIds,
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "statuses", required = false) List<OrderItemStatus> statuses) {
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
        return ServiceResponse.succeed(HttpStatus.OK, orderService.getOrderItemStatistic(orderItemQuery));
    }
}

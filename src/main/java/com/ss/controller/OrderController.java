package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.OrderRequest;
import com.ss.dto.response.OrderResponse;
import com.ss.dto.response.ServiceResponse;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.repository.query.OrderItemQuery;
import com.ss.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping
    PageResponse<OrderResponse> searchOrder(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "fromDate", required = false) Long fromDate,
            @RequestParam(name = "toDate", required = false) Long toDate,
            @RequestParam(name = "createdUser", required = false) String createdUser,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, orderService.searchOrder(code, status, fromDate, toDate, createdUser, pageCriteria));
    }

    @GetMapping("/item")
    PageResponse<OrderItemModel> searchOrderItem(
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

}

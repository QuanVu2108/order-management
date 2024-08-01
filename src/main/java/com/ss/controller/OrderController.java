package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemReceivedMultiRequest;
import com.ss.dto.request.OrderItemReceivedRequest;
import com.ss.dto.request.OrderItemUpdatedRequest;
import com.ss.dto.request.OrderRequest;
import com.ss.dto.response.*;
import com.ss.enums.ExportFormat;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.repository.query.OrderItemQuery;
import com.ss.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.ss.util.CommonUtil.convertSqlSearchText;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    ServiceResponse<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.createOrder(request));
    }

    @PutMapping("/{orderId}")
    ServiceResponse<OrderResponse> updateOrder(
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

    @PostMapping("/export")
    ResponseEntity<Resource> export(
            @RequestParam(name = "ids", required = false) List<UUID> ids,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "statuses", required = false) List<OrderStatus> statuses,
            @RequestParam(name = "fromDate", required = false) Long fromDate,
            @RequestParam(name = "toDate", required = false) Long toDate,
            @RequestParam(name = "createdUser", required = false) String createdUser) {
        ExportFormat format = ExportFormat.EXCEL;
        Resource resource = orderService.exportOrder(ids, code, statuses, fromDate, toDate, createdUser);
        String fileName = "order";
        try {
            return ResponseEntity
                    .ok()
                    .contentLength(resource.contentLength())
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=" + fileName + format.getExtension())
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            @RequestParam(name = "orderItemCode", required = false) String orderItemCode,
            @RequestParam(name = "orderIds", required = false) List<UUID> orderIds,
            @RequestParam(name = "orderCode", required = false) String orderCode,
            @RequestParam(name = "productIds", required = false) List<Long> productIds,
            @RequestParam(name = "productCode", required = false) String productCode,
            @RequestParam(name = "productNumber", required = false) String productNumber,
            @RequestParam(name = "storeIds", required = false) List<UUID> storeIds,
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "statuses", required = false) List<OrderItemStatus> statuses,
            @Valid PageCriteria pageCriteria) {
        OrderItemQuery orderItemQuery = OrderItemQuery.builder()
                .ids(ids)
                .orderIds(orderIds)
                .orderItemCode(convertSqlSearchText(orderItemCode))
                .orderCode(convertSqlSearchText(orderCode))
                .productCode(convertSqlSearchText(productCode))
                .productNumber(convertSqlSearchText(productNumber))
                .productIds(productIds)
                .store(convertSqlSearchText(store))
                .storeIds(storeIds)
                .statuses(statuses)
                .build();
        return PageResponse.succeed(HttpStatus.OK, orderService.searchOrderItem(orderItemQuery, pageCriteria));
    }

    @PostMapping("/order-item/export")
    ResponseEntity<Resource> exportOrderItem(
            @RequestParam(name = "ids", required = false) List<UUID> ids,
            @RequestParam(name = "orderItemCode", required = false) String orderItemCode,
            @RequestParam(name = "orderIds", required = false) List<UUID> orderIds,
            @RequestParam(name = "orderCode", required = false) String orderCode,
            @RequestParam(name = "productIds", required = false) List<Long> productIds,
            @RequestParam(name = "productCode", required = false) String productCode,
            @RequestParam(name = "productNumber", required = false) String productNumber,
            @RequestParam(name = "storeIds", required = false) List<UUID> storeIds,
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "statuses", required = false) List<OrderItemStatus> statuses) {
        ExportFormat format = ExportFormat.EXCEL;
        OrderItemQuery orderItemQuery = OrderItemQuery.builder()
                .ids(ids)
                .orderIds(orderIds)
                .orderItemCode(orderItemCode)
                .orderCode(convertSqlSearchText(orderCode))
                .productCode(convertSqlSearchText(productCode))
                .productNumber(convertSqlSearchText(productNumber))
                .productIds(productIds)
                .store(convertSqlSearchText(store))
                .storeIds(storeIds)
                .statuses(statuses)
                .build();
        Resource resource = orderService.exportOrderItem(orderItemQuery);
        String fileName = "order item";
        try {
            return ResponseEntity
                    .ok()
                    .contentLength(resource.contentLength())
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=" + fileName + format.getExtension())
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/order-item/update")
    ServiceResponse<List<OrderItemResponse>> updateOrderItem(
            @RequestBody @Valid OrderItemUpdatedRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.updateItemByUpdating(request));
    }

    @PutMapping("/order-item/receive/{orderItemId}")
    ServiceResponse<OrderItemResponse> receiveOrderItem(
            @PathVariable @Valid UUID orderItemId,
            @RequestBody @Valid OrderItemReceivedRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.receiveItem(orderItemId, request));
    }

    @PutMapping("/order-item/cancel/{orderItemId}")
    ServiceResponse<OrderItemResponse> cancelOrderItem(@PathVariable @Valid UUID orderItemId) {
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
                .orderCode(convertSqlSearchText(orderCode))
                .productCode(convertSqlSearchText(productCode))
                .productIds(productIds)
                .store(convertSqlSearchText(store))
                .storeIds(storeIds)
                .statuses(statuses)
                .build();
        return ServiceResponse.succeed(HttpStatus.OK, orderService.getOrderItemStatistic(orderItemQuery));
    }
}

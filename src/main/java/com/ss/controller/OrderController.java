package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.response.ServiceResponse;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    ServiceResponse<OrderModel> createOrder(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "content", required = false) String content) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.createOrder(title, content));
    }

    @PutMapping("/{orderId}")
    ServiceResponse<OrderModel> updateOrder(
            @PathVariable @Valid UUID orderId,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "content", required = false) String content) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.updateOrder(orderId, title, content));
    }

    @GetMapping
    PageResponse<OrderModel> searchOrder(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, orderService.searchOrder(keyword, status, pageCriteria));
    }

    @PostMapping("/item")
    ServiceResponse<OrderItemModel> createOrderItem(
            @RequestParam(name = "orderId", required = false) UUID orderId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "content", required = false) String content,
            @RequestParam(name = "quantityOrder", required = false) Long quantityOrder,
            @RequestParam(name = "quantityReality", required = false) Long quantityReality,
            @RequestParam(name = "priceOrder", required = false) Double priceOrder,
            @RequestParam(name = "priceReality", required = false) Double priceReality,
            @RequestParam(name = "status", required = false) OrderItemStatus status,
            @RequestParam(name = "warehouseId", required = false) UUID warehouseId,
            @RequestParam(name = "fileRequest", required = false) MultipartFile fileRequest) {
        OrderItemRequest request = OrderItemRequest.builder()
                .orderId(orderId)
                .name(name)
                .content(content)
                .quantityOrder(quantityOrder)
                .quantityReality(quantityReality)
                .priceOrder(priceOrder)
                .priceReality(priceReality)
                .status(status)
                .warehouseId(warehouseId)
                .build();
        return ServiceResponse.succeed(HttpStatus.OK, orderService.createOrderItem(request, fileRequest));
    }

    @PutMapping("/item/{orderItemId}")
    ServiceResponse<OrderItemModel> updateOrderItem(
            @PathVariable @Valid UUID orderItemId,
            @RequestParam(name = "orderId", required = false) UUID orderId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "content", required = false) String content,
            @RequestParam(name = "quantityOrder", required = false) Long quantityOrder,
            @RequestParam(name = "quantityReality", required = false) Long quantityReality,
            @RequestParam(name = "priceOrder", required = false) Double priceOrder,
            @RequestParam(name = "priceReality", required = false) Double priceReality,
            @RequestParam(name = "status", required = false) OrderItemStatus status,
            @RequestParam(name = "warehouseId", required = false) UUID warehouseId,
            @RequestParam(name = "fileRequest", required = false) MultipartFile fileRequest) {
        OrderItemRequest request = OrderItemRequest.builder()
                .orderId(orderId)
                .name(name)
                .content(content)
                .quantityOrder(quantityOrder)
                .quantityReality(quantityReality)
                .priceOrder(priceOrder)
                .priceReality(priceReality)
                .status(status)
                .warehouseId(warehouseId)
                .build();
        return ServiceResponse.succeed(HttpStatus.OK, orderService.updateOrderItem(orderItemId, request, fileRequest));
    }

    @GetMapping("/item")
    PageResponse<OrderItemModel> searchOrderItem(
            @RequestParam(name = "orderId", required = false) UUID orderId,
            @RequestParam(name = "warehouseId", required = false) UUID warehouseId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) OrderItemStatus status,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, orderService.searchOrderItem(orderId, warehouseId, keyword, status, pageCriteria));
    }

}

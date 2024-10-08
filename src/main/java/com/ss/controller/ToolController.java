package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemSubmittedRequest;
import com.ss.dto.request.OrderItemToolRequest;
import com.ss.dto.response.*;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.repository.query.OrderItemQuery;
import com.ss.service.OrderService;
import com.ss.service.ProductService;
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

    private final ProductService productService;

    @GetMapping("/order")
    ServiceResponse<List<OrderToolResponse>> searchOrder(
            @RequestParam(name = "ids", required = false) List<UUID> ids,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "statuses", required = false) List<OrderStatus> statuses,
            @RequestParam(name = "fromDate", required = false) Long fromDate,
            @RequestParam(name = "toDate", required = false) Long toDate,
            @RequestParam(name = "createdUser", required = false) String createdUser) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.searchListOrder(ids, code, statuses, fromDate, toDate, createdUser));
    }

    @GetMapping("/order-item")
    PageResponse<OrderItemResponse> getOrderItem(
            @RequestParam(name = "ids", required = false) List<UUID> ids,
            @RequestParam(name = "orderIds", required = false) List<UUID> orderIds,
            @RequestParam(name = "orderCode", required = false) String orderCode,
            @RequestParam(name = "productIds", required = false) List<Long> productIds,
            @RequestParam(name = "productCode", required = false) String productCode,
            @RequestParam(name = "productNumber", required = false) String productNumber,
            @RequestParam(name = "storeIds", required = false) List<UUID> storeIds,
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "statuses", required = false) List<OrderItemStatus> statuses,
            @RequestParam(name = "isGetInCart", required = false) Boolean isGetInCart,
            @Valid PageCriteria pageCriteria) {

        OrderItemQuery orderItemQuery = OrderItemQuery.builder()
                .ids(ids)
                .orderIds(orderIds)
                .orderCode(orderCode)
                .productCode(productCode)
                .productNumber(productNumber)
                .productIds(productIds)
                .store(store)
                .storeIds(storeIds)
                .statuses(statuses)
                .orderStatus(OrderStatus.PENDING)
                .isGetInCart(isGetInCart)
                .build();
        return PageResponse.succeed(HttpStatus.OK, orderService.searchOrderItem(orderItemQuery, pageCriteria));
    }

    @GetMapping("/order-by-store")
    ServiceResponse<List<OrderItemByStoreResponse>> getOrderByStore(@RequestParam(name = "statuses", required = false) List<OrderItemStatus> statuses) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.getOrderByStore(statuses));
    }

    @PutMapping("/order-item/{orderItemId}")
    ServiceResponse<OrderItemResponse> updateOrderItem(
            @PathVariable @Valid UUID orderItemId,
            @RequestBody @Valid OrderItemToolRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.updateOrderItemByTool(orderItemId, request));
    }

    @PostMapping("/order-item/submit")
    ServiceResponse<List<OrderItemResponse>> submitOrderItem(
            @RequestBody @Valid OrderItemSubmittedRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.submitByTool(request));
    }

    @GetMapping("/store-order/in-cart")
    ServiceResponse<List<OrderItemByStoreResponse>> getStoreOrderByInCart() {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.getStoreOrderByInCart());
    }

    @GetMapping("/sale-info/product")
    ServiceResponse<ProductSaleResponse> getProductInfoBySale(long productId) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.getProductInfoBySale(productId));
    }

}

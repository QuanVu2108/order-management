package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.OrderItemSubmittedRequest;
import com.ss.dto.response.ServiceResponse;
import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderItemModel;
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
            @RequestParam(name = "orderId", required = false) UUID orderId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status") OrderItemStatus status) {
        PageCriteria pageCriteria = PageCriteria.builder()
                .pageIndex(1)
                .pageSize(50)
                .build();
        return PageResponse.succeed(HttpStatus.OK, orderService.searchOrderItem(orderId, keyword, status, pageCriteria));
    }

    @PutMapping("/order-item/{orderItemId}")
    ServiceResponse<OrderItemModel> updateOrderItem(
            @PathVariable @Valid UUID orderItemId,
            @RequestBody @Valid OrderItemRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.updateOrderItem(orderItemId, request, null));
    }

    @PostMapping("/order-item/submit")
    ServiceResponse<List<OrderItemModel>> submitOrderItem(
            @RequestBody @Valid OrderItemSubmittedRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.submitByTool(request));
    }

}

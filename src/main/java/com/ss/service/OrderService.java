package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.OrderItemSubmittedRequest;
import com.ss.dto.request.OrderItemToolRequest;
import com.ss.dto.request.OrderRequest;
import com.ss.dto.response.OrderResponse;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.repository.query.OrderItemQuery;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderModel createOrder(OrderRequest request);

    OrderModel updateOrder(UUID orderId, OrderRequest request);

    PageResponse<OrderResponse> searchOrder(String code, OrderStatus status, Long fromDate, Long toDate, String createdUser, PageCriteria pageCriteria);

    PageResponse<OrderItemModel> searchOrderItem(OrderItemQuery orderItemQuery, PageCriteria pageCriteria);

    List<OrderItemModel> submitByTool(OrderItemSubmittedRequest request);

    OrderItemModel updateOrderItemByTool(UUID orderItemId, OrderItemToolRequest request);
}

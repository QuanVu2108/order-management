package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.OrderItemSubmittedRequest;
import com.ss.dto.response.OrderItemResponse;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderModel createOrder(String title, String content);

    OrderModel updateOrder(UUID orderId, String title, String content);

    OrderItemModel createOrderItem(OrderItemRequest request, MultipartFile fileRequest);

    OrderItemModel updateOrderItem(UUID orderItemId, OrderItemRequest request, MultipartFile fileRequest);

    PageResponse<OrderModel> searchOrder(String keyword, OrderStatus status, PageCriteria pageCriteria);

    PageResponse<OrderItemResponse> searchOrderItem(UUID orderId, UUID storeId, String keyword, OrderItemStatus status, PageCriteria pageCriteria);

    List<OrderItemModel> submitByTool(OrderItemSubmittedRequest request);

    OrderItemModel updateOrderItemByTool(UUID orderItemId, OrderItemRequest request);
}

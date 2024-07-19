package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.*;
import com.ss.dto.response.*;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.repository.query.OrderItemQuery;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    OrderResponse updateOrder(UUID orderId, OrderRequest request);

    PageResponse<OrderResponse> searchOrder(List<UUID> ids, String code, List<OrderStatus> statuses, Long fromDate, Long toDate, String createdUser, PageCriteria pageCriteria);

    Resource exportOrder(List<UUID> ids, String code, List<OrderStatus> statuses, Long fromDate, Long toDate, String createdUser);

    void receiveItemMulti(UUID orderId, List<OrderItemReceivedMultiRequest> request);

    PageResponse<OrderItemResponse> searchOrderItem(OrderItemQuery orderItemQuery, PageCriteria pageCriteria);

    Resource exportOrderItem(OrderItemQuery orderItemQuery);

    List<OrderItemModel> submitByTool(OrderItemSubmittedRequest request);

    OrderItemModel updateOrderItemByTool(UUID orderItemId, OrderItemToolRequest request);

    List<OrderToolResponse> searchListOrder(List<UUID> ids, String code, List<OrderStatus> statuses, Long fromDate, Long toDate, String createdUser);

    OrderStatisticResponse getStatistic(List<UUID> ids, String code, List<OrderStatus> statuses, Long fromDate, Long toDate, String createdUser);

    OrderItemModel receiveItem(UUID orderItemId, OrderItemReceivedRequest request);

    List<OrderItemByStoreResponse> getOrderByStore(List<OrderItemStatus> statuses);

    OrderItemModel cancelItem(UUID orderItemId);

    OrderItemStatisticResponse getOrderItemStatistic(OrderItemQuery orderItemQuery);

    List<OrderItemByStoreResponse> getStoreOrderByInCart();

    List<OrderItemModel> updateItemByUpdating(OrderItemUpdatedRequest request);

}

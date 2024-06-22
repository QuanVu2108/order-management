package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.OrderItemReceivedMultiRequest;
import com.ss.dto.request.OrderItemReceivedRequest;
import com.ss.dto.request.StoreItemDetailRequest;
import com.ss.dto.request.StoreItemRequest;
import com.ss.dto.response.StoreItemResponse;
import com.ss.enums.StoreItemType;
import com.ss.model.OrderItemModel;
import com.ss.model.ProductModel;
import com.ss.model.StoreItemModel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface StoreItemService {
    List<StoreItemModel> create(StoreItemRequest request);

    void createMulti(List<OrderItemModel> orderItems, List<OrderItemReceivedMultiRequest> orderItemReceivedRequests);

    List<StoreItemModel> update(UUID id, StoreItemDetailRequest request);

    void delete(UUID id);

    PageResponse<StoreItemResponse> search(String product, List<Long> productIds, String store, UUID order, StoreItemType type, Long fromTime, Long toTime, PageCriteria pageCriteria);

    List<StoreItemModel> findByProduct(ProductModel id);
}

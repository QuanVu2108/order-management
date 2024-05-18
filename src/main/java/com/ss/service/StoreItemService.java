package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.StoreItemDetailRequest;
import com.ss.dto.request.StoreItemRequest;
import com.ss.enums.StoreItemType;
import com.ss.model.StoreItemModel;

import java.util.List;
import java.util.UUID;

public interface StoreItemService {
    List<StoreItemModel> create(StoreItemRequest request);

    List<StoreItemModel> update(UUID id, StoreItemDetailRequest request);

    void delete(UUID id);

    PageResponse<StoreItemModel> search(String product, String store, UUID order, StoreItemType type, Long fromTime, Long toTime, PageCriteria pageCriteria);

}

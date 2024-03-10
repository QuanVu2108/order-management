package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.WarehouseRequest;
import com.ss.model.WarehouseModel;

import java.util.UUID;

public interface WarehouseService {
    WarehouseModel create(WarehouseRequest request);

    WarehouseModel update(UUID id, WarehouseRequest request);

    void delete(UUID id);

    PageResponse<WarehouseModel> search(String keyword, PageCriteria pageCriteria);

    WarehouseModel findById(UUID id);
}

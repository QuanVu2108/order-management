package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.ProductPropertyRequest;
import com.ss.enums.ProductPropertyType;
import com.ss.model.ProductPropertyModel;

import java.util.UUID;

public interface ProductPropertyService {
    ProductPropertyModel create(ProductPropertyRequest request);

    ProductPropertyModel update(UUID id, ProductPropertyRequest request);

    void delete(UUID id);

    PageResponse<ProductPropertyModel> search(String code, String name, ProductPropertyType type, PageCriteria pageCriteria);
}

package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.ProductRequest;
import com.ss.model.ProductModel;
import com.ss.model.ProductPropertyModel;

import java.util.UUID;

public interface ProductService {
    ProductModel create(ProductRequest request);

    ProductModel update(long id, ProductRequest request);

    void delete(long id);

    PageResponse<ProductModel> search(String code, String number, String name, String category, String brand, String color, String size, PageCriteria pageCriteria);
}

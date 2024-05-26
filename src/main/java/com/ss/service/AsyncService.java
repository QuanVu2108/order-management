package com.ss.service;

import com.ss.model.OrderModel;
import com.ss.model.ProductModel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AsyncService {
    void updateStatusOrders(OrderModel order, List<UUID> orderItemIds);

    void generateQRCodeProduct(List<ProductModel> products);

    void createImageProduct(List<ProductModel> updatedProducts, Map<String, List<String>> productImageUrlOriginal);
}

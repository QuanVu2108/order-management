package com.ss.service;

import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;

import java.util.List;

public interface AsyncService {
    void updateStatusOrders(OrderModel order, List<OrderItemModel> orderItems);
}

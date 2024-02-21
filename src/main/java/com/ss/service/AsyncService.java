package com.ss.service;

import com.ss.model.OrderModel;

import java.util.List;
import java.util.UUID;

public interface AsyncService {
    void updateStatusOrders(OrderModel order, List<UUID> orderItemIds);
}

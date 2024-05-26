package com.ss.service;

import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TelegramBotService {
    void sendOrder(OrderModel order);
    void sendOrderItems(List<OrderItemModel> orderItems, Map<UUID, Long> quantityMap);

    void sendOrderItem(OrderItemModel orderItem, OrderItemStatus status);
}

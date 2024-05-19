package com.ss.service;

import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;

import java.util.List;

public interface TelegramBotService {
    void sendOrder(OrderModel order);
    void sendOrderItems(List<OrderItemModel> orderItems);

    void sendOrderItem(OrderItemModel orderItem, OrderItemStatus status);
}

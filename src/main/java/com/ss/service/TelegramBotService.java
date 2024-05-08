package com.ss.service;

import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;

public interface TelegramBotService {
    void sendOrder(OrderModel order);

    void sendOrderItem(OrderItemModel orderItem, OrderItemStatus status);
}

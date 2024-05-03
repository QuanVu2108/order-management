package com.ss.service;

import com.ss.model.OrderModel;

public interface TelegramBotService {
    void sendOrder(OrderModel order);
}

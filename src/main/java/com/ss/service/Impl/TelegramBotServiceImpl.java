package com.ss.service.Impl;

import com.ss.client.telegram.Bot;
import com.ss.model.FileModel;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramBotServiceImpl implements TelegramBotService {

    private final Bot bot;

    @Override
    public void sendOrder(OrderModel order) {
        StringBuilder message = new StringBuilder("Order ");
        message.append(order.getCode());
        message.append(" was sent!!!");
        bot.sendMessage(message.toString());
        order.getItems().forEach(orderItem -> {
            sendOrderPhoto(orderItem);
        });
    }

    private void sendOrderPhoto(OrderItemModel orderItem) {
        StringBuilder message = new StringBuilder("");
        message.append("Store: " + orderItem.getStore().getName() + "\n ");
        message.append("Color: " + orderItem.getProduct().getColor() + "\n ");
        message.append("Size: " + orderItem.getProduct().getSize() + "\n ");
        message.append("Cost: " + orderItem.getCost() + "\n ");
        message.append("Quantity: " + orderItem.getQuantityOrder() + "\n ");
        bot.sendMessage(message.toString());
        if (orderItem.getProduct().getImages() != null && !orderItem.getProduct().getImages().isEmpty()) {
            List<String> imagePaths = orderItem.getProduct().getImages().stream()
                    .filter(item -> StringUtils.hasText(item.getUrl()))
                    .map(FileModel::getUrl)
                    .collect(Collectors.toList());
            bot.sendPhoto(imagePaths);
        }
    }
}

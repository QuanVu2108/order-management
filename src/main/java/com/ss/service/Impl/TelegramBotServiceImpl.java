package com.ss.service.Impl;

import com.ss.client.telegram.Bot;
import com.ss.enums.OrderItemStatus;
import com.ss.model.FileModel;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ss.enums.Const.DATE_FORMATTER;
import static com.ss.enums.Const.DATE_TITLE_FORMATTER;
import static com.ss.util.DateUtils.timestampToString;
import static com.ss.util.FileUtil.createPdfWithTableAndImage;
import static com.ss.util.FileUtil.downloadImage;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramBotServiceImpl implements TelegramBotService {

    private final Bot bot;

    @Override
    @Async
    public void sendOrder(OrderModel order) {
        StringBuilder message = new StringBuilder("Order ");
        message.append(order.getCode());
        message.append(" was sent!!!");
        bot.sendMessage(message.toString());
        List<List<String>> tableData = new ArrayList<>();
        List<String> tableTitles = List.of("STT", "image", "info");
        tableData.add(tableTitles);
        List<byte[]> imageBytes = new ArrayList<>();
        List<OrderItemModel> orderItems = order.getItems();
        for (int i = 0; i < orderItems.size(); i++) {
            List<String> tableColumns = new ArrayList<>();
            tableColumns.add(String.valueOf(i));
            tableColumns.add("image_" + i);

            OrderItemModel orderItem = orderItems.get(i);
            StringBuilder fileData = new StringBuilder("");
            fileData.append("Store: " + orderItem.getStore().getName() + "\n ");
            fileData.append("Number: " + orderItem.getProduct().getProductNumber() + "\n ");
            fileData.append("Color: " + orderItem.getProduct().getColor() + "\n ");
            fileData.append("Size: " + orderItem.getProduct().getSize() + "\n ");
            fileData.append("Cost: " + orderItem.getCost() + "\n ");
            fileData.append("Quantity: " + orderItem.getQuantityOrder() + "\n ");
            tableColumns.add(fileData.toString());
            tableData.add(tableColumns);
            if (orderItem.getProduct().getImages() != null && !orderItem.getProduct().getImages().isEmpty()) {
                FileModel file = new ArrayList<>(orderItem.getProduct().getImages()).get(0);
                imageBytes.add(downloadImage(file.getUrl()));
            } else
                imageBytes.add(new byte[0]);

        }
        String title = "order_" + order.getCode();
        File file = createPdfWithTableAndImage(title, tableData, imageBytes);
        bot.sendDocument(file);
    }

    @Override
    @Async
    public void sendOrderItems(List<OrderItemModel> orderItems) {
        bot.sendMessage("China side sent list product");
        List<List<String>> tableData = new ArrayList<>();
        List<String> tableTitles = List.of("STT", "image", "info");
        tableData.add(tableTitles);
        List<byte[]> imageBytes = new ArrayList<>();
        for (int i = 0; i < orderItems.size(); i++) {
            List<String> tableColumns = new ArrayList<>();
            tableColumns.add(String.valueOf(i));
            tableColumns.add("image_" + i);

            OrderItemModel orderItem = orderItems.get(i);
            StringBuilder fileData = new StringBuilder("");
            fileData.append("Order: " + orderItem.getOrderModel().getCode() + "\n ");
            fileData.append("Number: " + orderItem.getProduct().getProductNumber() + "\n ");
            fileData.append("Store: " + orderItem.getStore().getName() + "\n ");
            fileData.append("Color: " + orderItem.getProduct().getColor() + "\n ");
            fileData.append("Size: " + orderItem.getProduct().getSize() + "\n ");
            fileData.append("Cost: " + orderItem.getCost() + "\n ");
            fileData.append("Quantity: " + orderItem.getQuantityOrder() + "\n ");
            tableColumns.add(fileData.toString());
            tableData.add(tableColumns);
            if (orderItem.getProduct().getImages() != null && !orderItem.getProduct().getImages().isEmpty()) {
                FileModel file = new ArrayList<>(orderItem.getProduct().getImages()).get(0);
                imageBytes.add(downloadImage(file.getUrl()));
            } else
                imageBytes.add(new byte[0]);

        }
        String title = "item_" + timestampToString(DATE_TITLE_FORMATTER, System.currentTimeMillis());
        File file = createPdfWithTableAndImage(title, tableData, imageBytes);
        bot.sendDocument(file);
    }

    @Override
    @Async
    public void sendOrderItem(OrderItemModel orderItem, OrderItemStatus status) {
        StringBuilder message = new StringBuilder("");
        message.append("Order Item " + orderItem.getProduct().getInfo() + " was " + status.name() + " \n ");
        if (status.equals(OrderItemStatus.DELAY))
            message.append("Delay time: " + timestampToString(DATE_FORMATTER, orderItem.getDelayDay()));
        else if (status.equals(OrderItemStatus.UPDATING)) {
            message.append("Update Request \n ");
            message.append(" - Cost: " + orderItem.getCost() + " -> " + orderItem.getCostReality() + " \n ");
            message.append(" - Quantity: " + orderItem.getQuantityOrder() + " -> " + orderItem.getQuantityReality() + " \n ");
        } else
            return;
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

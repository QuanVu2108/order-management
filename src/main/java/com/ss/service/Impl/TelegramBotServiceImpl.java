package com.ss.service.Impl;

import com.ss.client.telegram.Bot;
import com.ss.enums.OrderItemStatus;
import com.ss.model.FileModel;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.model.ProductModel;
import com.ss.service.TelegramBotService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
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
        Map<Long, Long> itemCountMap = new HashMap<>();
        Set<ProductModel> productSet = new HashSet<>();
        orderItems.forEach(orderItem -> {
            ProductModel product = orderItem.getProduct();
            Long cnt = itemCountMap.get(product.getId()) == null ? 0 : itemCountMap.get(product.getId());
            itemCountMap.put(product.getId(), cnt + orderItem.getQuantityOrder());
            productSet.add(product);
        });
        List<ProductModel> products = new ArrayList<>(productSet);
        for (int i = 0; i < products.size(); i++) {
            List<String> tableColumns = new ArrayList<>();
            tableColumns.add(String.valueOf(i));
            tableColumns.add("image_" + i);

            ProductModel product = products.get(i);
            StringBuilder fileData = new StringBuilder("");
            fileData.append("Number: " + product.getProductNumber() + "\n ");
            fileData.append("Color: " + product.getColor() + "\n ");
            fileData.append("Size: " + product.getSize() + "\n ");
            fileData.append("Quantity: " + itemCountMap.get(product.getId()) + "\n ");
            tableColumns.add(fileData.toString());
            tableData.add(tableColumns);
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                FileModel file = new ArrayList<>(product.getImages()).get(0);
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
    public void sendOrderItems(List<OrderItemModel> orderItems, Map<UUID, Long> quantityMap) {
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
            fileData.append("Quantity: " + quantityMap.get(orderItem.getId()) + "\n ");
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

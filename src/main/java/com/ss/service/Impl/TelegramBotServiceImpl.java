package com.ss.service.Impl;

import com.ss.client.telegram.Bot;
import com.ss.dto.response.FileResponse;
import com.ss.dto.response.OrderItemResponse;
import com.ss.dto.response.ProductResponse;
import com.ss.enums.Const;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.enums.excel.OrderItemTelegramExcel;
import com.ss.model.*;
import com.ss.repository.OrderItemRepository;
import com.ss.service.ProductService;
import com.ss.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.ss.enums.Const.DATE_FORMATTER;
import static com.ss.enums.Const.DATE_TITLE_FORMATTER;
import static com.ss.util.CommonUtil.convertToString;
import static com.ss.util.DateUtils.timestampToString;
import static com.ss.util.FileUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramBotServiceImpl implements TelegramBotService {

    private final Bot bot;

    private final OrderItemRepository orderItemRepository;

    private final ProductService productService;

    @Value("${gcp.server}")
    private String serverGcp;

    @Value("${gcp.domain}")
    private String domainGcp;

    @Override
    @Async
    @Transactional
    public void sendOrder(OrderModel order) {
        log.info("********* sending order");
        StringBuilder message = new StringBuilder("Order ");
        message.append(order.getCode());
        if (OrderStatus.CHECKING.equals(order.getStatus()))
            message.append(" was sent to checking !!!");
        else if (OrderStatus.PENDING.equals(order.getStatus()))
            message.append(" was confirmed and in processing!!!");
        else
            return;
        bot.sendMessage(message.toString());
        List<List<String>> tableData = new ArrayList<>();
        List<String> tableTitles = List.of("STT", "image", "info");
        tableData.add(tableTitles);
        List<byte[]> imageBytes = new ArrayList<>();
        List<OrderItemModel> orderItems = order.getItems();
        List<ProductModel> productModels = orderItemRepository.findProductsByOrders(Arrays.asList(order));
        List<ProductResponse> products = productService.enrichProductResponse(productModels);
        Map<Long, Long> itemCountMap = new HashMap<>();
        orderItems.forEach(orderItem -> {
            ProductResponse product = products.stream()
                    .filter(item -> item.getId() == orderItem.getProduct().getId())
                    .findFirst().orElse(null);
            if (product != null) {
                Long cnt = itemCountMap.get(product.getId()) == null ? 0 : itemCountMap.get(product.getId());
                itemCountMap.put(product.getId(), cnt + orderItem.getQuantityOrder());
            }
        });
        Integer productCnt = 0;
        Integer orderItemCnt = 0;
        for (Long productId : itemCountMap.keySet()) {
            ProductResponse product = products.stream()
                    .filter(item -> item.getId() == productId)
                    .findFirst().orElse(null);
            if (product == null)
                continue;

            List<String> tableColumns = new ArrayList<>();
            tableColumns.add(String.valueOf(productCnt + 1));
            tableColumns.add("image_" + productCnt);

            StringBuilder fileData = new StringBuilder("");
            fileData.append("Number: " + product.getProductNumber() + "\n ");
            fileData.append("Color: " + product.getColor() + "\n ");
            fileData.append("Size: " + product.getSize() + "\n ");
            fileData.append("Quantity: " + itemCountMap.get(product.getId()) + "\n ");
            tableColumns.add(fileData.toString());
            tableData.add(tableColumns);
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                FileResponse file = new ArrayList<>(product.getImages()).get(0);
                imageBytes.add(downloadImage(file.getUrl(), domainGcp));
            } else
                imageBytes.add(new byte[0]);
            orderItemCnt += Math.toIntExact(itemCountMap.get(product.getId()));
            productCnt++;
        }

        String title = "order_" + order.getCode() + "_" + timestampToString(Const.DATE_TITLE_FORMATTER, System.currentTimeMillis());
        List<String> data = new ArrayList<>();
        data.add("Order : " + order.getCode());
        data.add("Total products : " + productCnt);
        data.add("Total product items : " + orderItemCnt);
        try {
            File pdfFile = createPdfWithTableAndImage(title, data, tableData, imageBytes);
            bot.sendDocument(pdfFile);
        } catch (Exception ex) {
            log.error("*************** create pdf file failed");
        }

        List<OrderItemResponse> responses = new ArrayList<>();
        orderItems.forEach(orderItem -> {
            OrderItemResponse response = new OrderItemResponse(orderItem);
            ProductResponse product = products.stream()
                    .filter(item -> item.getId() == orderItem.getProduct().getId())
                    .findFirst().orElse(null);
            response.setProduct(product);

            StoreModel store = orderItem.getStore();
            response.setStore(store);
            responses.add(response);
        });
        List<Map<String, String>> assets = getOrderItemAssets(responses);
        File excelFile = createExcelFile(assets, OrderItemTelegramExcel.values(), "order_item");
        bot.sendDocument(excelFile);
        log.info("********* sending order successfully");
    }

    private List<Map<String, String>> getOrderItemAssets(List<OrderItemResponse> data) {
        List<Map<String, String>> result = new ArrayList<>();
        int count = 1;
        for (OrderItemResponse orderItem : data) {
            Map<String, String> map = new HashMap<>();
            map.put(OrderItemTelegramExcel.STT.getKey(), String.valueOf(count++));
            map.put(OrderItemTelegramExcel.PRODUCT_NUMBER.getKey(), orderItem.getProduct().getProductNumber());
            ProductResponse product = orderItem.getProduct();
            if (product != null) {
                String imageUrl = product.getImages().stream().map(FileResponse::getUrl).collect(Collectors.joining(","));
                map.put(OrderItemTelegramExcel.IMAGE_URL.getKey(), imageUrl);
                map.put(OrderItemTelegramExcel.COLOR.getKey(), product.getColor());
                map.put(OrderItemTelegramExcel.SIZE.getKey(), product.getSize());
            }
            map.put(OrderItemTelegramExcel.QUANTITY.getKey(), convertToString(orderItem.getQuantityOrder()));
            result.add(map);
        }
        return result;
    }

    @Override
    @Async
    @Transactional
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
                imageBytes.add(downloadImage(file.getUrl(), domainGcp));
            } else
                imageBytes.add(new byte[0]);

        }
        String title = "item_" + timestampToString(DATE_TITLE_FORMATTER, System.currentTimeMillis());
        File file = createPdfWithTableAndImage(title, new ArrayList<>(), tableData, imageBytes);
        bot.sendDocument(file);
    }

    @Override
    @Async
    @Transactional
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

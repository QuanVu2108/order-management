package com.ss.service.Impl;

import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.model.ProductModel;
import com.ss.repository.OrderItemRepository;
import com.ss.repository.OrderRepository;
import com.ss.repository.ProductRepository;
import com.ss.service.AsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ss.util.QRCodeUtil.generateQRCodeImage;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncServiceImpl implements AsyncService {

    private final OrderItemRepository orderItemRepository;

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    @Override
    @Async
    public void updateStatusOrders(OrderModel order, List<UUID> submittedOrderItemIds) {
        List<OrderItemModel> allOrderItems = orderItemRepository.findByOrderModel(order);
        List<OrderItemModel> unsubmittedOrderItems = allOrderItems.stream()
                .filter(item -> !submittedOrderItemIds.contains(item.getId()) && OrderItemStatus.PENDING.equals(item.getStatus()))
                .collect(Collectors.toList());
        if (unsubmittedOrderItems.isEmpty()) {
            order.setStatus(OrderStatus.DONE);
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);
        }
    }

    @Override
    public void generateQRCodeProduct(List<ProductModel> products) {
        if (products != null && !products.isEmpty()) {
            products.forEach(product -> {
                product.setQrCode(generateQRCodeImage(String.valueOf(product.getId())));
            });
            productRepository.saveAll(products);
        }
    }

}

package com.ss.service.Impl;

import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.repository.OrderItemRepository;
import com.ss.repository.OrderRepository;
import com.ss.service.AsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncServiceImpl implements AsyncService {

    private final OrderItemRepository orderItemRepository;

    private final OrderRepository orderRepository;

    @Override
    @Async
    public void updateStatusOrders(OrderModel order, List<UUID> submittedOrderItemIds) {
        List<OrderItemModel> allOrderItems = orderItemRepository.findByOrderModel(order);
        List<OrderItemModel> unsubmittedOrderItems = allOrderItems.stream()
                .filter(item -> !submittedOrderItemIds.contains(item.getId()) && OrderItemStatus.PENDING.equals(item.getStatus()))
                .collect(Collectors.toList());
        if (unsubmittedOrderItems.isEmpty()) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);
        }
    }

}

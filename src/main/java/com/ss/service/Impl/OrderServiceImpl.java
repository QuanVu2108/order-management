package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.OrderItemRequest;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.exception.ExceptionResponse;
import com.ss.model.FileModel;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.repository.FileRepository;
import com.ss.repository.OrderItemRepository;
import com.ss.repository.OrderRepository;
import com.ss.service.OrderService;
import com.ss.util.StorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final StorageUtil storageUtil;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    private final FileRepository fileRepository;

    @Override
    @Transactional
    public OrderModel createOrder(String title, String content) {
        long orderCnt = orderRepository.count();
        String code = String.valueOf(orderCnt + 1);
        OrderModel order = OrderModel.builder()
                .id(UUID.randomUUID())
                .title(title)
                .content(content)
                .code(code)
                .build();
        orderRepository.save(order);
        return order;
    }

    @Override
    @Transactional
    public OrderModel updateOrder(UUID orderId, String title, String content) {

        Optional<OrderModel> orderModelOptional = orderRepository.findById(orderId);
        if (orderModelOptional.isEmpty())
            throw new ExceptionResponse("order is not existed!!!");
        OrderModel order = orderModelOptional.get();
        order.setTitle(title);
        order.setContent(content);
        orderRepository.save(order);
        return order;
    }

    @Override
    public OrderItemModel createOrderItem(OrderItemRequest request, MultipartFile fileRequest) {
        OrderModel order = null;
        if (request.getOrderId() == null) {
            order = createOrder(null, null);
            orderRepository.save(order);
        } else {
            Optional<OrderModel> orderModelOptional = orderRepository.findById(request.getOrderId());
            if (orderModelOptional.isEmpty())
                throw new ExceptionResponse("order is not existed!!!");
            order = orderModelOptional.get();
        }
        OrderItemModel orderItem = new OrderItemModel();
        orderItem.update(request);
        orderItem.setOrderModel(order);
        orderItemRepository.save(orderItem);

        if (fileRequest != null) {
            FileModel file = storageUtil.uploadFile(fileRequest);
            file.setOrderItem(orderItem);
            fileRepository.save(file);
        }
        return orderItem;
    }

    @Override
    public OrderItemModel updateOrderItem(UUID orderItemId, OrderItemRequest request, MultipartFile fileRequest) {

        Optional<OrderItemModel> orderItemModelOptional = orderItemRepository.findById(orderItemId);
        if (orderItemModelOptional.isEmpty())
            throw new ExceptionResponse("order item is not existed!!!");
        OrderItemModel orderItem = orderItemModelOptional.get();
        orderItem.update(request);

        if (fileRequest != null) {
            FileModel file = storageUtil.uploadFile(fileRequest);
            file.setOrderItem(orderItem);
            fileRepository.save(file);
        }
        return orderItem;
    }

    @Override
    public PageResponse<OrderModel> searchOrder(String keyword, OrderStatus status, PageCriteria pageCriteria) {
        if (keyword != null)
            keyword = "%" + keyword.toUpperCase() + "%";
        Page<OrderModel> orderPage = orderRepository.search(keyword, status, pageCriteriaPageableMapper.toPageable(pageCriteria));

        return PageResponse.<OrderModel>builder()
                .paging(Paging.builder().totalCount(orderPage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(orderPage.getContent())
                .build();
    }

    @Override
    public PageResponse<OrderItemModel> searchOrderItem(UUID orderId, String keyword, OrderItemStatus status, PageCriteria pageCriteria) {
        if (keyword != null)
            keyword = "%" + keyword.toUpperCase() + "%";
        List<UUID> itemIds = null;
        if (orderId != null) {
            Optional<OrderModel> orderModel = orderRepository.findById(orderId);
            if (orderModel.isEmpty())
                throw new ExceptionResponse("order is not existed!!!");
            List<OrderItemModel> orderItems = orderItemRepository.findByOrderModel(orderModel.get());
            itemIds = orderItems.stream().map(OrderItemModel::getId).collect(Collectors.toList());
        }
        Page<OrderItemModel> orderPage = orderItemRepository.searchItem(itemIds, keyword, status, pageCriteriaPageableMapper.toPageable(pageCriteria));

        return PageResponse.<OrderItemModel>builder()
                .paging(Paging.builder().totalCount(orderPage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(orderPage.getContent())
                .build();
    }
}

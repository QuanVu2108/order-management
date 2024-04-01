package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.OrderItemSubmittedRequest;
import com.ss.dto.response.OrderItemResponse;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.exception.ExceptionResponse;
import com.ss.model.FileModel;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.model.StoreModel;
import com.ss.repository.FileRepository;
import com.ss.repository.OrderItemRepository;
import com.ss.repository.OrderRepository;
import com.ss.service.AsyncService;
import com.ss.service.OrderService;
import com.ss.service.StoreService;
import com.ss.util.StorageUtil;
import com.ss.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    private final AsyncService asyncService;

    private final StoreService storeService;

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
        order = orderRepository.save(order);
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
        order.setUpdatedAt(Instant.now());
        order = orderRepository.save(order);
        return order;
    }

    @Override
    public OrderItemModel createOrderItem(OrderItemRequest request) {
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
        if (request.getStoreId() != null) {
            StoreModel store = storeService.findById(request.getStoreId());
            if (store == null)
                throw new ExceptionResponse("store is not existed!!!");
        }
        OrderItemModel orderItem = new OrderItemModel();
        orderItem.update(request);
        orderItem.setOrderModel(order);
        orderItem = orderItemRepository.save(orderItem);
        return orderItem;
    }

    @Override
    public OrderItemModel updateOrderItem(UUID orderItemId, OrderItemRequest request) {
        Optional<OrderItemModel> orderItemModelOptional = orderItemRepository.findById(orderItemId);
        if (orderItemModelOptional.isEmpty())
            throw new ExceptionResponse("order item is not existed!!!");
        OrderItemModel orderItem = orderItemModelOptional.get();
        if (request.getStoreId() != orderItem.getStoreId() && request.getStoreId() != null) {
            StoreModel store = storeService.findById(request.getStoreId());
            if (store == null)
                throw new ExceptionResponse("store is not existed!!!");
        }

        orderItem.update(request);
        orderItem = orderItemRepository.save(orderItem);
        return orderItem;
    }

    @Override
    public PageResponse<OrderModel> searchOrder(String keyword, OrderStatus status, PageCriteria pageCriteria) {
        keyword = StringUtil.convertSqlSearchText(keyword);
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
    @Transactional
    public PageResponse<OrderItemResponse> searchOrderItem(UUID orderId, UUID storeId, String keyword, OrderItemStatus status, PageCriteria pageCriteria) {
        keyword = StringUtil.convertSqlSearchText(keyword);
        List<UUID> itemIds = null;
        if (orderId != null) {
            Optional<OrderModel> orderModel = orderRepository.findById(orderId);
            if (orderModel.isEmpty())
                throw new ExceptionResponse("order is not existed!!!");
            List<OrderItemModel> orderItems = orderItemRepository.findByOrderModel(orderModel.get());
            itemIds = orderItems.stream().map(OrderItemModel::getId).collect(Collectors.toList());
        }
        Page<OrderItemModel> orderPage = orderItemRepository.searchItem(itemIds, storeId, keyword, status, pageCriteriaPageableMapper.toPageable(pageCriteria));
        List<OrderItemModel> orderItemModels = orderPage.getContent();
        List<UUID> storeIds = orderItemModels.stream()
                .filter(item -> item.getStoreId() != null)
                .map(OrderItemModel::getStoreId)
                .collect(Collectors.toList());
        Set<StoreModel> stores = storeService.findByIds(storeIds);
        List<OrderItemResponse> responses = new ArrayList<>();
        orderItemModels.forEach(orderItemModel -> {
            StoreModel store = stores.stream()
                    .filter(item -> orderItemModel.getStoreId() != null && orderItemModel.getStoreId().equals(item.getId()))
                    .findFirst().orElse(null);
            responses.add(new OrderItemResponse(orderItemModel, store));
        });

        return PageResponse.<OrderItemResponse>builder()
                .paging(Paging.builder().totalCount(orderPage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(responses)
                .build();
    }

    @Override
    public OrderItemModel updateOrderItemByTool(UUID orderItemId, OrderItemRequest request) {
        Optional<OrderItemModel> orderItemModelOptional = orderItemRepository.findById(orderItemId);
        if (orderItemModelOptional.isEmpty())
            throw new ExceptionResponse("order item is not existed!!!");
        OrderItemModel orderItem = orderItemModelOptional.get();

        orderItem.updateByTool(request);
        orderItem = orderItemRepository.save(orderItem);

        return orderItem;
    }

    @Override
    public List<OrderItemModel> submitByTool(OrderItemSubmittedRequest request) {
        List<OrderItemModel> orderItems = orderItemRepository.findAllById(request.getIds());
        Set<UUID> orderIds = new HashSet<>();
        orderItems.forEach(item -> {
            item.setStatus(OrderItemStatus.OK);
            item.setUpdatedAt(Instant.now());
            orderIds.add(item.getOrderModel().getId());
        });
        orderItems = orderItemRepository.saveAll(orderItems);

        List<OrderModel> orders = orderRepository.findAllById(orderIds);
        orders.forEach(order -> asyncService.updateStatusOrders(order, request.getIds()));

        return orderItems;
    }

}

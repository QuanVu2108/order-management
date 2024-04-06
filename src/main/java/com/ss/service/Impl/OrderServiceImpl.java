package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.OrderItemSubmittedRequest;
import com.ss.dto.request.OrderItemToolRequest;
import com.ss.dto.request.OrderRequest;
import com.ss.dto.response.OrderResponse;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.exception.ExceptionResponse;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.model.StoreModel;
import com.ss.model.UserModel;
import com.ss.repository.OrderItemRepository;
import com.ss.repository.OrderRepository;
import com.ss.repository.query.OrderItemQuery;
import com.ss.repository.query.UserQuery;
import com.ss.service.*;
import com.ss.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    private final ProductService productService;

    private final UserService userService;

    @Override
    @Transactional
    public OrderModel createOrder(OrderRequest request) {
        String orderCode = genOrderCode();
        OrderModel order = new OrderModel(orderCode);
        order.update(request);
        order = orderRepository.save(order);
        createOrderItem(request.getItems(), order);
        return order;
    }

    private String genOrderCode() {
        LocalDate currentDate = LocalDate.now();
        // Define date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // Format date as string
        String dateString = currentDate.format(formatter);
        long orderCnt = orderRepository.countByDate(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()) + 1;
        return dateString + "_" + orderCnt;
    }

    @Override
    @Transactional
    public OrderModel updateOrder(UUID orderId, OrderRequest request) {
        Optional<OrderModel> orderModelOptional = orderRepository.findById(orderId);
        if (orderModelOptional.isEmpty())
            throw new ExceptionResponse("order is not existed!!!");
        OrderModel order = orderModelOptional.get();
        List<OrderItemRequest> itemRequests = request.getItems();
        List<OrderItemRequest> newItems = new ArrayList<>();
        List<OrderItemRequest> updatingItems = new ArrayList<>();
        List<UUID> updatingItemIds = new ArrayList<>();
        itemRequests.forEach(itemRequest -> {
            if (itemRequest.getId() == null) newItems.add(itemRequest);
            else {
                updatingItems.add(itemRequest);
                updatingItemIds.add(itemRequest.getId());
            }
        });
        // create new order item
        List<OrderItemModel> orderItems = createOrderItem(newItems, order);

        // update order item
        List<UUID> storeIds = updatingItems.stream()
                .filter(item -> item.getStoreId() != null)
                .map(OrderItemRequest::getStoreId)
                .collect(Collectors.toList());
        Set<StoreModel> stores = storeService.findByIds(storeIds);

        List<OrderItemModel> existedItems = orderItemRepository.findAllById(updatingItemIds);
        existedItems.forEach(existedItem -> {
            OrderItemRequest itemRequest = updatingItems.stream()
                    .filter(item -> item.getId().equals(existedItem.getId()))
                    .findFirst().orElse(null);
            if (itemRequest != null) {
                StoreModel store = stores.stream()
                        .filter(item -> itemRequest.getStoreId() != null && item.getId().equals(itemRequest.getStoreId()))
                        .findFirst().orElse(null);
                if (store == null)
                    throw new ExceptionResponse("store is invalid by product " + itemRequest.getProductId());
                existedItem.update(itemRequest, store);
                orderItems.add(existedItem);
            }
        });

        // delete order item


        orderItemRepository.saveAll(orderItems);
        order.update(request);
        order = orderRepository.save(order);
        return order;
    }

    public List<OrderItemModel> createOrderItem(List<OrderItemRequest> itemRequests, OrderModel order) {
        List<UUID> storeIds = itemRequests.stream()
                .filter(item -> item.getStoreId() != null)
                .map(OrderItemRequest::getStoreId)
                .collect(Collectors.toList());
        Set<StoreModel> stores = storeService.findByIds(storeIds);

        List<OrderItemModel> orderItems = new ArrayList<>();
        int index = 1;
        for (int i = 0; i < itemRequests.size(); i++) {
            OrderItemRequest itemRequest = itemRequests.get(i);
            OrderItemModel orderItem = new OrderItemModel(order, index);
            StoreModel store = stores.stream()
                    .filter(item -> itemRequest.getStoreId() != null && item.getId().equals(itemRequest.getStoreId()))
                    .findFirst().orElse(null);
            if (store == null)
                throw new ExceptionResponse("store is invalid at row " + i);
            orderItem.update(itemRequest, store);
            orderItems.add(orderItem);
            index++;
        }
        orderItems = orderItemRepository.saveAll(orderItems);
        return orderItems;
    }

    @Override
    public PageResponse<OrderResponse> searchOrder(String code, OrderStatus status, Long fromDate, Long toDate, String createdUser, PageCriteria pageCriteria) {
        code = StringUtil.convertSqlSearchText(code);
        List<String> createdUsers = null;
        List<UserModel> users = new ArrayList<>();
        if (StringUtils.hasText(createdUser)) {
            UserQuery userQuery = UserQuery.builder()
                    .keyword(createdUser)
                    .build();
            users = userService.searchList(userQuery);
            createdUsers = users.stream()
                    .map(UserModel::getUsername)
                    .collect(Collectors.toList());
        }
        Page<OrderModel> orderPage = orderRepository.search(code, status, fromDate, toDate, createdUsers, pageCriteriaPageableMapper.toPageable(pageCriteria));
        List<OrderModel> orders = orderPage.getContent();

        List<OrderResponse> responses = new ArrayList<>();
        if (users.isEmpty()) {
            Set<String> userNames = orders.stream()
                    .filter(item -> StringUtils.hasText(item.getCreatedBy()))
                    .map(OrderModel::getCreatedBy)
                    .collect(Collectors.toSet());
            UserQuery userQuery = UserQuery.builder()
                    .userNames(userNames)
                    .build();
            users = userService.searchList(userQuery);
        }

        for (int i = 0; i < orders.size(); i++) {
            OrderModel order = orders.get(i);
            UserModel user = users.stream()
                    .filter(item -> StringUtils.hasText(item.getCreatedBy()) && item.getCreatedBy().equals(order.getCreatedBy()))
                    .findFirst().orElse(null);
            OrderResponse response = new OrderResponse(order, user);
            responses.add(response);
        }

        return PageResponse.<OrderResponse>builder()
                .paging(Paging.builder().totalCount(orderPage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(responses)
                .build();
    }

    @Override
    @Transactional
    public PageResponse<OrderItemModel> searchOrderItem(OrderItemQuery orderItemQuery, PageCriteria pageCriteria) {
        Page<OrderItemModel> orderPage = orderItemRepository.search(orderItemQuery, pageCriteriaPageableMapper.toPageable(pageCriteria));
        return PageResponse.<OrderItemModel>builder()
                .paging(Paging.builder().totalCount(orderPage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(orderPage.getContent())
                .build();
    }

    @Override
    public OrderItemModel updateOrderItemByTool(UUID orderItemId, OrderItemToolRequest request) {
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
        OrderItemStatus status = request.getStatus();
        orderItems.forEach(item -> {
            item.setStatus(status);
            item.setUpdatedAt(Instant.now());
        });
        orderItems = orderItemRepository.saveAll(orderItems);
        return orderItems;
    }
}

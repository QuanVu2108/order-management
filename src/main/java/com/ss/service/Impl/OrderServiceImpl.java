package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.*;
import com.ss.dto.response.*;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.exception.ExceptionResponse;
import com.ss.model.*;
import com.ss.repository.OrderItemRepository;
import com.ss.repository.OrderRepository;
import com.ss.repository.query.OrderItemQuery;
import com.ss.repository.query.OrderQuery;
import com.ss.repository.query.UserQuery;
import com.ss.service.*;
import com.ss.service.mapper.OrderItemMapper;
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

    private final OrderItemMapper orderItemMapper;

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
            if (request.getStatus().equals(OrderStatus.CANCEL))
                itemRequest.setStatus(OrderItemStatus.CANCEL);

            if (itemRequest.getId() == null)
                newItems.add(itemRequest);
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

        List<Long> productIds = itemRequests.stream()
                .filter(item -> item.getProductId() != null)
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());
        List<ProductModel> products = productService.findByIds(productIds);

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
                    throw new ExceptionResponse("store is invalid by product " + itemRequest.getStoreId());

                ProductModel product = products.stream()
                        .filter(item -> itemRequest.getProductId() != null && item.getId() == itemRequest.getProductId())
                        .findFirst().orElse(null);
                if (product == null)
                    throw new ExceptionResponse("product is invalid at row " + itemRequest.getProductId());

                existedItem.update(itemRequest, store, product);
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

        List<Long> productIds = itemRequests.stream()
                .filter(item -> item.getProductId() != null)
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());
        List<ProductModel> products = productService.findByIds(productIds);

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

            ProductModel product = products.stream()
                    .filter(item -> itemRequest.getProductId() != null && item.getId() == itemRequest.getProductId())
                    .findFirst().orElse(null);
            if (product == null)
                throw new ExceptionResponse("product is invalid at row " + i);

            orderItem.update(itemRequest, store, product);
            orderItems.add(orderItem);
            index++;
        }
        orderItems = orderItemRepository.saveAll(orderItems);
        return orderItems;
    }

    @Override
    public PageResponse<OrderResponse> searchOrder(List<UUID> ids, String code, List<OrderStatus> statuses, Long fromDate, Long toDate, String createdUser, PageCriteria pageCriteria) {
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
        OrderQuery query = OrderQuery.builder()
                .ids(ids)
                .code(StringUtil.convertSqlSearchText(code))
                .statuses(statuses)
                .fromDate(fromDate)
                .toDate(toDate)
                .createdUsers(createdUsers)
                .build();
        Page<OrderModel> orderPage = orderRepository.search(query, pageCriteriaPageableMapper.toPageable(pageCriteria));
        List<OrderModel> orders = orderPage.getContent();
        List<OrderResponse> responses = enrichOrderResponse(orders, users);
        return PageResponse.<OrderResponse>builder()
                .paging(Paging.builder().totalCount(orderPage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(responses)
                .build();
    }

    private List<OrderResponse> enrichOrderResponse(List<OrderModel> orders, List<UserModel> users) {
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
                    .filter(item -> StringUtils.hasText(order.getCreatedBy()) && order.getCreatedBy().equals(item.getUsername()))
                    .findFirst().orElse(null);
            OrderResponse response = new OrderResponse(order, user);
            responses.add(response);
        }
        return responses;
    }

    @Override
    @Transactional
    public PageResponse<OrderItemResponse> searchOrderItem(OrderItemQuery orderItemQuery, PageCriteria pageCriteria) {
        Page<OrderItemModel> orderPage = orderItemRepository.search(orderItemQuery, pageCriteriaPageableMapper.toPageable(pageCriteria));
        List<OrderItemModel> orderItems = orderPage.getContent();
        List<OrderItemResponse> responses = new ArrayList<>();
        orderItems.forEach(orderItem -> {
            OrderItemResponse response = orderItemMapper.toTarget(orderItem);
            response.setOrder(new OrderResponse(orderItem.getOrderModel(), null));
            responses.add(response);
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
    @Transactional
    public List<OrderToolResponse> searchListOrder(List<UUID> ids, String code, List<OrderStatus> statuses, Long fromDate, Long toDate, String createdUser) {
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
        OrderQuery query = OrderQuery.builder()
                .ids(ids)
                .code(StringUtil.convertSqlSearchText(code))
                .statuses(statuses)
                .fromDate(fromDate)
                .toDate(toDate)
                .createdUsers(createdUsers)
                .build();
        List<OrderModel> orders = orderRepository.searchList(query);
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

        List<OrderToolResponse> responses = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            OrderModel order = orders.get(i);
            UserModel user = users.stream()
                    .filter(item -> StringUtils.hasText(order.getCreatedBy()) && order.getCreatedBy().equals(item.getUsername()))
                    .findFirst().orElse(null);
            OrderItemStatisticResponse statistic = enrichItemStatistic(order.getItems());
            OrderResponse orderResponse = new OrderResponse(order, user);
            OrderToolResponse response = OrderToolResponse.builder()
                    .statistic(statistic)
                    .order(orderResponse)
                    .build();
            responses.add(response);
        }
        return responses;
    }

    private OrderItemStatisticResponse enrichItemStatistic(List<OrderItemModel> items) {
        int allCnt = 0;
        int pendingCnt = 0;
        int checkedCnt = 0;
        int delayCnt = 0;
        int updateCnt = 0;
        int sentCnt = 0;
        int inCartCnt = 0;
        int cancelCnt = 0;
        int doneCnt = 0;
        for (int i = 0; i < items.size(); i++) {
            allCnt++;
            OrderItemModel item = items.get(i);
            if (item.getStatus() != null) {
                if (item.getStatus().equals(OrderItemStatus.PENDING)) pendingCnt++;
                if (item.getStatus().equals(OrderItemStatus.CHECKED)) checkedCnt++;
                if (item.getStatus().equals(OrderItemStatus.DELAY)) delayCnt++;
                if (item.getStatus().equals(OrderItemStatus.UPDATE)) updateCnt++;
                if (item.getStatus().equals(OrderItemStatus.SENT)) sentCnt++;
                if (item.getStatus().equals(OrderItemStatus.IN_CART)) inCartCnt++;
                if (item.getStatus().equals(OrderItemStatus.CANCEL)) cancelCnt++;
                if (item.getStatus().equals(OrderItemStatus.DONE)) doneCnt++;
            }
        }
        return OrderItemStatisticResponse.builder()
                .allCnt(allCnt)
                .pendingCnt(pendingCnt)
                .checkedCnt(checkedCnt)
                .delayCnt(delayCnt)
                .updateCnt(updateCnt)
                .sentCnt(sentCnt)
                .inCartCnt(inCartCnt)
                .cancelCnt(cancelCnt)
                .doneCnt(doneCnt)
                .build();

    }

    @Override
    public OrderStatisticResponse getStatistic(List<UUID> ids, String code, List<OrderStatus> statuses, Long fromDate, Long toDate, String createdUser) {
        List<String> createdUsers = null;
        if (StringUtils.hasText(createdUser)) {
            UserQuery userQuery = UserQuery.builder()
                    .keyword(createdUser)
                    .build();
            List<UserModel> users = userService.searchList(userQuery);
            createdUsers = users.stream()
                    .map(UserModel::getUsername)
                    .collect(Collectors.toList());
        }
        OrderQuery query = OrderQuery.builder()
                .ids(ids)
                .code(StringUtil.convertSqlSearchText(code))
                .statuses(statuses)
                .fromDate(fromDate)
                .toDate(toDate)
                .createdUsers(createdUsers)
                .build();
        List<OrderModel> orders = orderRepository.searchList(query);
        int allCnt = 0;
        int newCnt = 0;
        int pendingCnt = 0;
        int doneCnt = 0;
        for (int i = 0; i < orders.size(); i++) {
            allCnt++;
            OrderModel order = orders.get(i);
            if (order.getStatus() != null) {
                if (order.getStatus().equals(OrderStatus.NEW)) newCnt++;
                if (order.getStatus().equals(OrderStatus.PENDING)) pendingCnt++;
                if (order.getStatus().equals(OrderStatus.DONE)) doneCnt++;
            }
        }
        return new OrderStatisticResponse(allCnt, newCnt, pendingCnt, doneCnt);
    }

    @Override
    @Transactional
    public OrderItemModel receiveItem(UUID orderItemId, OrderItemReceivedRequest request) {
        Optional<OrderItemModel> itemOptional = orderItemRepository.findById(orderItemId);
        if (itemOptional.isEmpty())
            throw new ExceptionResponse("order item is not existed!!!");
        OrderItemModel item = itemOptional.get();
        item.updateByReceive(request);
        item = orderItemRepository.save(item);
        if (item.getStatus() != null && item.getStatus().equals(OrderItemStatus.DONE)) {
            OrderModel order = item.getOrderModel();
            if (checkFinishOrder(order)) {
                order.setStatus(OrderStatus.DONE);
                orderRepository.save(order);
            }
        }
        return item;
    }

    @Override
    @Transactional
    public List<OrderItemByStoreResponse> getOrderByStore(List<OrderItemStatus> statuses) {
        List<OrderItemModel> orderItems = orderItemRepository.findByStatusIn(statuses);
        Set<OrderItemByStoreResponse> responses = orderItems.stream()
                .map(item -> new OrderItemByStoreResponse(item.getStore()))
                .collect(Collectors.toSet());
        orderItems.forEach(orderItem -> {
            OrderItemByStoreResponse response = responses.stream()
                    .filter(item -> item.getStore() != null && item.getStore().equals(orderItem.getStore()))
                    .findFirst().orElse(null);
            if (response != null) {
                response.updateOrderCnt();
                response.updateProductCnt(orderItem.getQuantityOrder());
            }
        });
        return new ArrayList<>(responses);
    }

    @Override
    public OrderItemModel cancelItem(UUID orderItemId) {
        Optional<OrderItemModel> itemOptional = orderItemRepository.findById(orderItemId);
        if (itemOptional.isEmpty())
            throw new ExceptionResponse("order item is not existed!!!");
        OrderItemModel item = itemOptional.get();
        item.setStatus(OrderItemStatus.CANCEL);
        item = orderItemRepository.save(item);
        updateOrderByItem(item);
        return item;
    }

    private void updateOrderByItem(OrderItemModel item) {
        OrderModel order = item.getOrderModel();
        List<OrderItemModel> orderItems = orderItemRepository.findByOrderModel(order);
        Long totalQuantity = Long.valueOf(0);
        Double totalCost = Double.valueOf(0);
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItemModel orderItem = orderItems.get(i);
            if (!orderItem.getStatus().equals(OrderItemStatus.CANCEL)) {
                totalQuantity += orderItem.getQuantityOrder();
                totalCost = orderItem.getCostTotal();
            }
        }
        order.setTotalQuantity(totalQuantity);
        order.setTotalCost(totalCost);
        Double incentive = order.getIncentive() == null ? 0 : order.getIncentive();
        order.setActualCost(totalCost - incentive);
        orderRepository.save(order);
    }

    private boolean checkFinishOrder(OrderModel orderModel) {
        List<OrderItemModel> orderItems = orderItemRepository.findByOrderModel(orderModel);
        List<OrderItemStatus> statuses = orderItems.stream().map(OrderItemModel::getStatus).collect(Collectors.toList());
        OrderItemStatus pendingStatus = statuses.stream()
                .filter(item -> OrderItemStatus.getPendingStatus().contains(item))
                .findFirst().orElse(null);
        if (pendingStatus == null)
            return true;
        return false;
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

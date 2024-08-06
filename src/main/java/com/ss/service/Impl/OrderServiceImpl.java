package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.*;
import com.ss.dto.response.*;
import com.ss.enums.Const;
import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.enums.StoreItemType;
import com.ss.enums.excel.OrderExportExcel;
import com.ss.enums.excel.OrderItemExportExcel;
import com.ss.exception.ExceptionResponse;
import com.ss.exception.http.InvalidInputError;
import com.ss.model.*;
import com.ss.repository.FileRepository;
import com.ss.repository.OrderItemRepository;
import com.ss.repository.OrderRepository;
import com.ss.repository.query.OrderItemQuery;
import com.ss.repository.query.OrderQuery;
import com.ss.repository.query.UserQuery;
import com.ss.service.*;
import com.ss.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.ss.enums.Const.*;
import static com.ss.util.DateUtils.instantToString;
import static com.ss.util.DateUtils.timestampToString;
import static com.ss.util.CommonUtil.convertToString;
import static com.ss.util.excel.ExcelUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    private final StoreService storeService;

    private final ProductService productService;

    private final UserService userService;

    private final StoreItemService storeItemService;

    private final FileRepository fileRepository;

    private final TelegramBotService telegramBotService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String orderCode = genOrderCode();
        OrderModel order = new OrderModel(orderCode);
        order.update(request);
        order.setStatus(OrderStatus.NEW);
        order = orderRepository.save(order);
        List<OrderItemModel> orderItems = createOrderItem(request.getItems(), order);
        order.setItems(orderItems);
        return enrichOrderResponse(List.of(order), List.of(userService.getUserInfo())).get(0);
    }

    private String genOrderCode() {
        LocalDate now = LocalDate.now();
        String monthVal = now.getMonthValue() < 10 ? ("0" + now.getMonthValue()) : String.valueOf(now.getMonthValue());
        long orderCnt = orderRepository.countByMonthAndYear(now.getMonthValue(), now.getYear()) + 1;
        return "PO" + "_" + monthVal + "_" + orderCnt;
    }

    @Override
    public OrderResponse updateOrder(UUID orderId, OrderRequest request) {
        Optional<OrderModel> orderModelOptional = orderRepository.findById(orderId);
        if (orderModelOptional.isEmpty())
            throw new ExceptionResponse(InvalidInputError.PRODUCT_NOT_FOUND.getMessage(), InvalidInputError.PRODUCT_NOT_FOUND);
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
        List<FileModel> files = fileRepository.findByProductIn(products);

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
                    throw new ExceptionResponse(InvalidInputError.STORE_INVALID.getMessage(),  InvalidInputError.STORE_INVALID);

                ProductModel product = products.stream()
                        .filter(item -> itemRequest.getProductId() != null && item.getId() == itemRequest.getProductId())
                        .findFirst().orElse(null);
                if (product == null)
                    throw new ExceptionResponse(InvalidInputError.PRODUCT_INVALID.getMessage(),  InvalidInputError.PRODUCT_INVALID);

                existedItem.update(itemRequest, store, product);
            } else {
                existedItem.setDeleted(true);
            }
            orderItems.add(existedItem);
        });

        order.update(request);
        order = orderRepository.save(order);
        List<OrderItemModel> updatedOrderItems = orderItemRepository.saveAll(orderItems);
        order.setItems(updatedOrderItems.stream()
                .filter(item -> !item.isDeleted())
                .collect(Collectors.toList()));
        telegramBotService.sendOrder(order);
        return enrichOrderResponse(List.of(order), List.of(userService.getUserInfo())).get(0);
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
                throw new ExceptionResponse(InvalidInputError.STORE_INVALID.getMessage(),  InvalidInputError.STORE_INVALID);

            ProductModel product = products.stream()
                    .filter(item -> itemRequest.getProductId() != null && item.getId() == itemRequest.getProductId())
                    .findFirst().orElse(null);
            if (product == null)
                throw new ExceptionResponse(InvalidInputError.PRODUCT_INVALID.getMessage(),  InvalidInputError.PRODUCT_INVALID);

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
                .code(CommonUtil.convertSqlSearchText(code))
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

    public List<OrderResponse> enrichOrderResponse(List<OrderModel> orders, List<UserModel> users) {
        List<OrderResponse> responses = new ArrayList<>();
        if (users.isEmpty()) {
            Set<String> userNames = orders.stream()
                    .filter(item -> StringUtils.hasText(item.getCreatedBy()))
                    .map(OrderModel::getCreatedBy)
                    .collect(Collectors.toSet());
            users = userService.findByUsernames(userNames);
        }

        List<OrderItemModel> allOrderItems = orderItemRepository.findByOrderModelIn(orders);
        List<ProductModel> allProducts = orderItemRepository.findProductsByOrders(orders);
        List<StoreModel> allStores = orderItemRepository.findStoresByOrders(orders);

        for (int i = 0; i < orders.size(); i++) {
            OrderModel order = orders.get(i);
            UserModel user = users.stream()
                    .filter(item -> StringUtils.hasText(order.getCreatedBy()) && order.getCreatedBy().equals(item.getUsername()))
                    .findFirst().orElse(null);

            List<OrderItemModel> orderItems = allOrderItems.stream()
                    .filter(item -> item.getOrderModel().getId().equals(order.getId()))
                    .collect(Collectors.toList());
            List<Long> productIds = orderItems.stream().map(item -> item.getProduct().getId()).collect(Collectors.toList());
            List<ProductModel> products = allProducts.stream()
                    .filter(item -> productIds.contains(item.getId()))
                    .collect(Collectors.toList());

            OrderResponse response = new OrderResponse(order, user, orderItems, products, allStores);
            responses.add(response);
        }
        return responses;
    }

    @Override
    public Resource exportOrder(List<UUID> ids, String code, List<OrderStatus> statuses, Long fromDate, Long toDate, String createdUser) {
        PageCriteria pageCriteria = PageCriteria.builder()
                .pageIndex(1)
                .pageSize(MAX_EXPORT_SIZE)
                .build();
        PageResponse<OrderResponse> orderPage = searchOrder(ids, code, statuses, fromDate, toDate, createdUser, pageCriteria);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Workbook workbook = null;
        try {
            workbook = getWorkbook(null, "order.xlsx");
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            Sheet sheet = workbook.createSheet("order");
            int rowHeaderIndex = 0;
            makeHeader(workbook, sheet, rowHeaderIndex, OrderExportExcel.values());

            List<Map<String, String>> areaAssets = getOrderAssets(orderPage.getData());
            makeContent(workbook, sheet, rowHeaderIndex, areaAssets, OrderExportExcel.values());
            autoSizeColumns(sheet, OrderExportExcel.values().length);

            workbook.write(byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray().clone();
            return new ByteArrayResource(bytes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionResponse(InvalidInputError.EXPORT_FILE_FAILED.getMessage(), InvalidInputError.EXPORT_FILE_FAILED);
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private List<Map<String, String>> getOrderAssets(List<OrderResponse> data) {
        List<Map<String, String>> result = new ArrayList<>();
        int count = 1;
        for (OrderResponse order : data) {
            Map<String, String> map = new HashMap<>();
            map.put(OrderExportExcel.STT.getKey(), String.valueOf(count++));
            map.put(OrderExportExcel.CODE.getKey(), order.getCode());
            map.put(OrderExportExcel.DATE.getKey(), timestampToString(Const.DATE_FORMATTER, order.getDate()));
            map.put(OrderExportExcel.STATUS.getKey(), order.getStatus() == null ? "" : order.getStatus().name());
            String productCodes = order.getItems().stream().map(item -> item.getProduct().getCode()).collect(Collectors.joining(","));
            String productNumbers = order.getItems().stream().map(item -> item.getProduct().getProductNumber()).collect(Collectors.joining(","));
            map.put(OrderExportExcel.PRODUCT_CODE.getKey(), productCodes);
            map.put(OrderExportExcel.PRODUCT_NUMBER.getKey(), productNumbers);
            map.put(OrderExportExcel.TOTAL_QUANTITY.getKey(), convertToString(order.getTotalQuantity()));
            map.put(OrderExportExcel.TOTAL_COST.getKey(), convertToString(order.getTotalCost()));
            map.put(OrderExportExcel.INCENTIVE.getKey(), convertToString(order.getIncentive()));
            map.put(OrderExportExcel.UPDATED_TIME.getKey(), instantToString(DATE_TIME_DETAIL_FORMATTER, order.getUpdatedAt()));
            map.put(OrderExportExcel.NOTE.getKey(), order.getNote());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public void receiveItemMulti(UUID orderId, List<OrderItemReceivedMultiRequest> request) {
        Optional<OrderModel> orderModelOptional = orderRepository.findById(orderId);
        if (orderModelOptional.isEmpty())
            throw new ExceptionResponse(InvalidInputError.ORDER_INVALID.getMessage(),  InvalidInputError.ORDER_INVALID);
        OrderModel order = orderModelOptional.get();
        List<OrderItemModel> orderItems = order.getItems();
        List<OrderItemModel> importOrderItems = new ArrayList<>();
        orderItems.forEach(item -> {
            OrderItemReceivedMultiRequest receivedItemRequest = request.stream()
                    .filter(itemRequest -> itemRequest.getId().equals(item.getId()))
                    .findFirst().orElse(null);
            if (receivedItemRequest != null && receivedItemRequest.getReceivedQuantity() != null && receivedItemRequest.getReceivedQuantity() > 0) {
                if (!OrderItemStatus.SENT.equals(item.getStatus()))
                    throw new ExceptionResponse(InvalidInputError.ORDER_ITEM_STATUS_INVALID.getMessage(), InvalidInputError.ORDER_ITEM_STATUS_INVALID);
                item.updateByReceive(receivedItemRequest);
                importOrderItems.add(item);
            }
        });

        OrderItemStatus pendingStatus = orderItems.stream()
                .map(OrderItemModel::getStatus)
                .filter(item -> OrderItemStatus.getPendingStatus().contains(item))
                .findFirst().orElse(null);
        if (pendingStatus == null)
            order.setStatus(OrderStatus.DONE);
        orderItemRepository.saveAll(orderItems);

        storeItemService.createMulti(orderItems, request);
    }

    @Override
    public PageResponse<OrderItemResponse> searchOrderItem(OrderItemQuery orderItemQuery, PageCriteria pageCriteria) {
        Page<OrderItemModel> orderPage = orderItemRepository.search(orderItemQuery, pageCriteriaPageableMapper.toPageable(pageCriteria));
        List<OrderItemModel> orderItems = orderPage.getContent();
        Set<Long> productIds = new HashSet<>();
        Set<UUID> storeIds = new HashSet<>();
        Set<UUID> orderIds = new HashSet<>();
        orderItems.forEach(orderItem -> {
            productIds.add(orderItem.getProduct().getId());
            storeIds.add(orderItem.getStore().getId());
            orderIds.add(orderItem.getOrderModel().getId());
        });
        List<ProductModel> productModels = productService.findByIds(new ArrayList<>(productIds));
        List<ProductResponse> products = productService.enrichProductResponse(productModels);
        Set<StoreModel> stores = storeService.findByIds(new ArrayList<>(storeIds));
        List<OrderModel> orders = orderRepository.findAllById(orderIds);

        List<OrderItemResponse> responses = new ArrayList<>();
        orderItems.forEach(orderItem -> {
            OrderItemResponse response = new OrderItemResponse(orderItem);
            OrderModel order = orders.stream()
                    .filter(item -> item.getId().equals(orderItem.getOrderModel().getId()))
                    .findFirst().orElse(null);
            if (order != null)
                response.setOrder(new BasicModelResponse(order.getId(), order.getCode(), null, order.getDate()));

            ProductResponse product = products.stream()
                    .filter(item -> item.getId() == orderItem.getProduct().getId())
                    .findFirst().orElse(null);
            response.setProduct(product);

            StoreModel store = stores.stream()
                    .filter(item -> item.getId().equals(orderItem.getStore().getId()))
                    .findFirst().orElse(null);
            response.setStore(store);
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
    public Resource exportOrderItem(OrderItemQuery orderItemQuery) {
        PageCriteria pageCriteria = PageCriteria.builder()
                .pageIndex(1)
                .pageSize(MAX_EXPORT_SIZE)
                .build();
        PageResponse<OrderItemResponse> orderItem = searchOrderItem(orderItemQuery, pageCriteria);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Workbook workbook = null;
        try {
            workbook = getWorkbook(null, "order_item.xlsx");
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            Sheet sheet = workbook.createSheet("order item");
            int rowHeaderIndex = 0;
            makeHeader(workbook, sheet, rowHeaderIndex, OrderItemExportExcel.values());

            List<Map<String, String>> areaAssets = getOrderItemAssets(orderItem.getData());
            makeContent(workbook, sheet, rowHeaderIndex, areaAssets, OrderItemExportExcel.values());
            autoSizeColumns(sheet, OrderItemExportExcel.values().length);

            workbook.write(byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray().clone();
            return new ByteArrayResource(bytes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionResponse(InvalidInputError.EXPORT_FILE_FAILED.getMessage(), InvalidInputError.EXPORT_FILE_FAILED);
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Map<String, String>> getOrderItemAssets(List<OrderItemResponse> data) {
        List<Map<String, String>> result = new ArrayList<>();
        int count = 1;
        for (OrderItemResponse orderItem : data) {
            Map<String, String> map = new HashMap<>();
            map.put(OrderItemExportExcel.STT.getKey(), String.valueOf(count++));
            map.put(OrderItemExportExcel.ORDER_CODE.getKey(), orderItem.getOrder().getCode());
            map.put(OrderItemExportExcel.PRODUCT_CODE.getKey(), orderItem.getProduct().getCode());
            map.put(OrderItemExportExcel.PRODUCT_NUMBER.getKey(), orderItem.getProduct().getProductNumber());
            String imageUrl = orderItem.getProduct().getImages().stream().map(FileResponse::getUrl).collect(Collectors.joining(","));
            map.put(OrderItemExportExcel.IMAGE_URL.getKey(), imageUrl);
            map.put(OrderItemExportExcel.STATUS.getKey(), orderItem.getStatus() == null ? "" : orderItem.getStatus().name());
            map.put(OrderItemExportExcel.DATE.getKey(), instantToString(DATE_FORMATTER, orderItem.getUpdatedAt()));
            map.put(OrderItemExportExcel.QUANTITY.getKey(), convertToString(orderItem.getQuantityOrder()));
            map.put(OrderItemExportExcel.COST.getKey(), convertToString(orderItem.getCost()));
            map.put(OrderItemExportExcel.TOTAL_COST.getKey(), convertToString(orderItem.getCostTotal()));
            map.put(OrderItemExportExcel.DELAY.getKey(), timestampToString(DATE_FORMATTER, orderItem.getDelayDay()));
            map.put(OrderItemExportExcel.NOTE.getKey(), orderItem.getNote());
            map.put(OrderItemExportExcel.CHECKED.getKey(), convertToString(orderItem.getQuantityChecked()));
            map.put(OrderItemExportExcel.IN_CART.getKey(), convertToString(orderItem.getQuantityChecked()));
            map.put(OrderItemExportExcel.RECEIVED.getKey(), convertToString(orderItem.getQuantityReceived()));
            map.put(OrderItemExportExcel.SENT.getKey(), convertToString(orderItem.getQuantitySent()));

            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public OrderItemModel updateOrderItemByTool(UUID orderItemId, OrderItemToolRequest request) {
        Optional<OrderItemModel> orderItemModelOptional = orderItemRepository.findById(orderItemId);
        if (orderItemModelOptional.isEmpty())
            throw new ExceptionResponse(InvalidInputError.ORDER_ITEM_INVALID.getMessage(),  InvalidInputError.ORDER_ITEM_INVALID);
        OrderItemModel orderItem = orderItemModelOptional.get();
        orderItem.updateByTool(request);

        if (request.getStatus() != null && (request.getStatus().equals(OrderItemStatus.DELAY) || request.getStatus().equals(OrderItemStatus.UPDATING)))
            telegramBotService.sendOrderItem(orderItem, request.getStatus());

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
                .code(CommonUtil.convertSqlSearchText(code))
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
            users = userService.findByUsernames(userNames);
        }

        List<OrderItemModel> allOrderItems = orderItemRepository.findByOrderModelIn(orders);
        List<ProductModel> allProducts = orderItemRepository.findProductsByOrders(orders);
        List<StoreModel> allStores = orderItemRepository.findStoresByOrders(orders);

        List<OrderToolResponse> responses = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            OrderModel order = orders.get(i);
            UserModel user = users.stream()
                    .filter(item -> StringUtils.hasText(order.getCreatedBy()) && order.getCreatedBy().equals(item.getUsername()))
                    .findFirst().orElse(null);
            OrderItemStatisticResponse statistic = enrichItemStatistic(order.getItems());

            List<OrderItemModel> orderItems = allOrderItems.stream()
                    .filter(item -> item.getOrderModel().getId().equals(order.getId()))
                    .collect(Collectors.toList());
            List<Long> productIds = orderItems.stream().map(item -> item.getProduct().getId()).collect(Collectors.toList());
            List<ProductModel> products = allProducts.stream()
                    .filter(item -> productIds.contains(item.getId()))
                    .collect(Collectors.toList());

            OrderResponse orderResponse = new OrderResponse(order, user, orderItems, products, allStores);
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
                if (item.getStatus().equals(OrderItemStatus.DELAY)) delayCnt++;
                if (item.getStatus().equals(OrderItemStatus.UPDATING)) updateCnt++;
                if (item.getStatus().equals(OrderItemStatus.CANCEL)) cancelCnt++;
                if (item.getStatus().equals(OrderItemStatus.DONE)) doneCnt++;
                if (item.getQuantityChecked() != null) checkedCnt += item.getQuantityChecked();
                if (item.getQuantitySent() != null) sentCnt += item.getQuantitySent();
                if (item.getQuantityInCart() != null) inCartCnt += item.getQuantityInCart();
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
                .code(CommonUtil.convertSqlSearchText(code))
                .statuses(statuses)
                .fromDate(fromDate)
                .toDate(toDate)
                .createdUsers(createdUsers)
                .build();
        List<OrderModel> orders = orderRepository.searchList(query);
        int allCnt = 0;
        int newCnt = 0;
        int checkingCnt = 0;
        int pendingCnt = 0;
        int doneCnt = 0;
        int cancelCnt = 0;
        for (int i = 0; i < orders.size(); i++) {
            allCnt++;
            OrderModel order = orders.get(i);
            if (order.getStatus() != null) {
                if (order.getStatus().equals(OrderStatus.NEW)) newCnt++;
                if (order.getStatus().equals(OrderStatus.CHECKING)) checkingCnt++;
                if (order.getStatus().equals(OrderStatus.PENDING)) pendingCnt++;
                if (order.getStatus().equals(OrderStatus.DONE)) doneCnt++;
                if (order.getStatus().equals(OrderStatus.CANCEL)) cancelCnt++;
            }
        }
        return new OrderStatisticResponse(allCnt, newCnt, checkingCnt, pendingCnt, doneCnt, cancelCnt);
    }

    @Override
    @Transactional
    public OrderItemResponse receiveItem(UUID orderItemId, OrderItemReceivedRequest request) {
        Optional<OrderItemModel> itemOptional = orderItemRepository.findById(orderItemId);
        if (itemOptional.isEmpty())
            throw new ExceptionResponse(InvalidInputError.ORDER_ITEM_INVALID.getMessage(),  InvalidInputError.ORDER_ITEM_INVALID);
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
        StoreItemDetailRequest storeItemDetail = StoreItemDetailRequest.builder()
                .productId(item.getProduct().getId())
                .quantity(request.getReceivedQuantity())
                .cost(item.getCost())
                .build();
        StoreItemRequest storeItemRequest = StoreItemRequest.builder()
                .storeId(item.getStore().getId())
                .type(StoreItemType.IMPORT)
                .orderId(item.getOrderModel().getId())
                .items(Arrays.asList(storeItemDetail))
                .build();
        storeItemService.create(storeItemRequest);
        return new OrderItemResponse(item);
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
                response.addOrder(orderItem.getOrderModel());
                response.updateProductCnt(orderItem.getQuantityOrder());
            }
        });
        return new ArrayList<>(responses);
    }

    @Override
    public OrderItemResponse cancelItem(UUID orderItemId) {
        Optional<OrderItemModel> itemOptional = orderItemRepository.findById(orderItemId);
        if (itemOptional.isEmpty())
            throw new ExceptionResponse(InvalidInputError.ORDER_ITEM_INVALID.getMessage(),  InvalidInputError.ORDER_ITEM_INVALID);
        OrderItemModel item = itemOptional.get();
        item.setStatus(OrderItemStatus.CANCEL);
        item = orderItemRepository.save(item);
        updateOrderByItem(item);
        return new OrderItemResponse(item);
    }

    @Override
    public OrderItemStatisticResponse getOrderItemStatistic(OrderItemQuery orderItemQuery) {
        List<OrderItemModel> orderItems = orderItemRepository.searchList(orderItemQuery);
        return enrichItemStatistic(orderItems);
    }

    @Override
    public List<OrderItemByStoreResponse> getStoreOrderByInCart() {
        List<OrderItemModel> orderItems = orderItemRepository.findByStatusInAndQuantityInCartGreaterThan(Arrays.asList(OrderItemStatus.PENDING), Long.valueOf(0));
        Set<OrderItemByStoreResponse> responses = orderItems.stream()
                .map(item -> new OrderItemByStoreResponse(item.getStore()))
                .collect(Collectors.toSet());
        orderItems.forEach(orderItem -> {
            OrderItemByStoreResponse response = responses.stream()
                    .filter(item -> item.getStore() != null && item.getStore().equals(orderItem.getStore()))
                    .findFirst().orElse(null);
            if (response != null) {
                response.addOrder(orderItem.getOrderModel());
                response.updateProductCnt(orderItem.getQuantityInCart());
            }
        });
        return new ArrayList<>(responses);
    }

    @Override
    public List<OrderItemResponse> updateItemByUpdating(OrderItemUpdatedRequest request) {
        boolean isApproved = request.isApproved();
        List<UUID> ids = request.getDetails().stream().map(OrderItemUpdatedDetailRequest::getId).collect(Collectors.toList());
        List<OrderItemModel> orderItems = orderItemRepository.findAllById(ids);

        Set<Long> productIds = new HashSet<>();
        Set<UUID> storeIds = new HashSet<>();
        if (isApproved) {
            request.getDetails().forEach(itemRequest -> {
                OrderItemModel orderItem = orderItems.stream()
                        .filter(item -> item.getId().equals(itemRequest.getId()))
                        .findFirst().orElse(null);
                if (orderItem != null) {
                    orderItem.updateItemByUpdating(itemRequest);
                    productIds.add(orderItem.getProduct().getId());
                    storeIds.add(orderItem.getStore().getId());
                }
            });
        } else {
            orderItems.forEach(item -> {
                item.setStatus(OrderItemStatus.CANCEL);
                productIds.add(item.getProduct().getId());
            });
        }
        List<OrderItemModel> updatedOrderItems = orderItemRepository.saveAll(orderItems);

        List<ProductModel> productModels = productService.findByIds(productIds);
        List<ProductResponse> products = productService.enrichProductResponse(productModels);

        Set<StoreModel> stores = storeService.findByIds(storeIds);

        List<OrderItemResponse> responses = new ArrayList<>();
        orderItems.forEach(orderItem -> {
            OrderItemResponse response = new OrderItemResponse(orderItem);

            ProductResponse product = products.stream()
                    .filter(item -> item.getId() == orderItem.getProduct().getId())
                    .findFirst().orElse(null);
            response.setProduct(product);

            StoreModel store = stores.stream()
                    .filter(item -> item.getId().equals(orderItem.getStore().getId()))
                    .findFirst().orElse(null);
            response.setStore(store);
            responses.add(response);
        });
        return responses;
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
    @Transactional
    public List<OrderItemModel> submitByTool(OrderItemSubmittedRequest request) {
        List<OrderItemModel> orderItems = orderItemRepository.findAllById(request.getIds());
        Map<UUID, Long> inCartOrderItem = new HashMap<>();
        orderItems.forEach(item -> {
            inCartOrderItem.put(item.getId(), item.getQuantityInCart());
            item.submitByTool();
        });
        telegramBotService.sendOrderItems(orderItems, inCartOrderItem);
        orderItems = orderItemRepository.saveAll(orderItems);
        return orderItems;
    }
}

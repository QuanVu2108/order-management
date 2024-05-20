package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.OrderItemReceivedMultiRequest;
import com.ss.dto.request.OrderItemReceivedRequest;
import com.ss.dto.request.StoreItemDetailRequest;
import com.ss.dto.request.StoreItemRequest;
import com.ss.dto.response.OrderResponse;
import com.ss.dto.response.StoreItemResponse;
import com.ss.enums.StoreItemType;
import com.ss.exception.ExceptionResponse;
import com.ss.exception.http.InvalidInputError;
import com.ss.exception.http.NotFoundError;
import com.ss.model.*;
import com.ss.repository.OrderRepository;
import com.ss.repository.ProductRepository;
import com.ss.repository.StoreItemRepository;
import com.ss.repository.StoreRepository;
import com.ss.repository.query.StoreItemQuery;
import com.ss.service.StoreItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.ss.util.StringUtil.convertSqlSearchText;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreItemServiceImpl implements StoreItemService {

    private final StoreItemRepository storeItemRepository;

    private final ProductRepository productRepository;

    private final StoreRepository storeRepository;

    private final OrderRepository orderRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    @Override
    public List<StoreItemModel> create(StoreItemRequest request) {
        Optional<StoreModel> storeOptional = storeRepository.findById(request.getStoreId());
        if (storeOptional.isEmpty())
            throw new ExceptionResponse(InvalidInputError.STORE_INVALID.getMessage(), InvalidInputError.STORE_INVALID);
        StoreModel store = storeOptional.get();

        Optional<StoreModel> targetStoreOptional = null;
        if (request.getTargetStore() != null) {
            targetStoreOptional = storeRepository.findById(request.getStoreId());
            if (targetStoreOptional.isEmpty())
                throw new ExceptionResponse(InvalidInputError.TARGET_STORE_INVALID.getMessage(), InvalidInputError.TARGET_STORE_INVALID);
        }
        StoreModel targetStore = request.getTargetStore() != null ? targetStoreOptional.get() : null;

        Optional<OrderModel> orderOptional = null;
        if (request.getOrderId() != null) {
            orderOptional = orderRepository.findById(request.getOrderId());
            if (orderOptional.isEmpty())
                throw new ExceptionResponse(InvalidInputError.ORDER_INVALID.getMessage(), InvalidInputError.ORDER_INVALID);

        }
        OrderModel order = request.getOrderId() != null ? orderOptional.get() : null;

        List<StoreItemModel> updatedStoreItems = new ArrayList<>();
        List<StoreItemModel> existedInventories = storeItemRepository.findByStoreAndType(store, StoreItemType.INVENTORY);
        List<Long> productIds = request.getItems().stream().map(StoreItemDetailRequest::getProductId).collect(Collectors.toList());
        List<ProductModel> products = productRepository.findAllById(productIds);
        request.getItems().forEach(requestItem -> {
            ProductModel product = products.stream()
                    .filter(item -> item.getId() == requestItem.getProductId())
                    .findFirst().orElse(null);
            if (product == null)
                throw new ExceptionResponse(InvalidInputError.PRODUCT_INVALID.getMessage(), InvalidInputError.PRODUCT_INVALID);

            StoreItemModel existedInventory = existedInventories.stream()
                    .filter(item -> item.getStore().getId().equals(store.getId()) && item.getProductId() == product.getId())
                    .findFirst().orElse(new StoreItemModel(store, product, StoreItemType.INVENTORY));

            StoreItemType type = request.getType();
            if (type.equals(StoreItemType.EXPORT) && existedInventory.getQuantity() < requestItem.getQuantity())
                throw new ExceptionResponse(InvalidInputError.EXPORT_QUANTITY_INVALID.getMessage(), InvalidInputError.EXPORT_QUANTITY_INVALID);

            StoreItemModel storeItem = new StoreItemModel(store, product, type);
            storeItem.update(requestItem, targetStore, order);
            updatedStoreItems.add(storeItem);

            existedInventory.updateInventory(type, requestItem.getQuantity());
            updatedStoreItems.add(existedInventory);
        });

        List<StoreItemModel> storeItems = storeItemRepository.saveAll(updatedStoreItems);
        return storeItems;
    }

    @Override
    @Async
    public void createMulti(List<OrderItemModel> orderItems, List<OrderItemReceivedMultiRequest> orderItemReceivedRequests) {
        Set<Long> products = new HashSet<>();
        Set<StoreModel> stores = new HashSet<>();
        orderItems.forEach(orderItem -> {
            products.add(orderItem.getProduct().getId());
            stores.add(orderItem.getStore());
        });
        List<StoreItemModel> existedInventories = storeItemRepository.findByStoreInAndProductIdInAndType(stores, products, StoreItemType.INVENTORY);
        List<StoreItemModel> storeItems = new ArrayList<>();
        orderItems.forEach(orderItem -> {
            OrderItemReceivedMultiRequest orderItemRequest = orderItemReceivedRequests.stream()
                    .filter(item -> item.getId().equals(orderItem.getId()))
                    .findFirst().orElse(null);
            if (orderItemRequest != null) {
                StoreModel store = orderItem.getStore();
                ProductModel product = orderItem.getProduct();
                if (store != null) {
                    StoreItemModel storeItem = new StoreItemModel(store, product, StoreItemType.IMPORT);
                    storeItem.setQuantity(orderItemRequest.getReceivedQuantity());
                    storeItem.setNote(orderItemRequest.getNote());
                    storeItem.setOrder(orderItem.getOrderModel());
                    storeItems.add(storeItem);

                    StoreItemModel inventory = existedInventories.stream()
                            .filter(item -> (item.getProductId() == product.getId()) && item.getStore().getId().equals(store.getId()))
                            .findFirst().orElse(new StoreItemModel(store, product, StoreItemType.INVENTORY));
                    inventory.updateInventory(StoreItemType.IMPORT, orderItemRequest.getReceivedQuantity());
                    storeItems.add(inventory);
                }
            }
        });
        storeItemRepository.saveAll(storeItems);
    }

    @Override
    public List<StoreItemModel> update(UUID id, StoreItemDetailRequest request) {
        Optional<StoreItemModel> storeItemOptional = storeItemRepository.findById(id);
        if (storeItemOptional.isEmpty())
            throw new ExceptionResponse(InvalidInputError.STORE_ITEM_INVALID.getMessage(), InvalidInputError.STORE_ITEM_INVALID);
        StoreItemModel storeItem = storeItemOptional.get();
        Long productId = storeItem.getProductId();
        Optional<ProductModel> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty())
            throw new ExceptionResponse(NotFoundError.PRODUCT_NOT_FOUND.getMessage(), NotFoundError.PRODUCT_NOT_FOUND);
        ProductModel product = productOptional.get();
        StoreModel store = storeItem.getStore();
        StoreItemType type = storeItem.getType();
        StoreItemModel inventory = storeItemRepository.findByStoreAndProductIdAndType(store, product.getId(), StoreItemType.INVENTORY);

        if (type.equals(StoreItemType.EXPORT) && (inventory.getQuantity() + storeItem.getQuantity() - request.getQuantity()) < 0)
            throw new ExceptionResponse(InvalidInputError.EXPORT_QUANTITY_INVALID.getMessage(), InvalidInputError.EXPORT_QUANTITY_INVALID);

        List<StoreItemModel> updatedStoreItems = new ArrayList<>();
        inventory.updateInventory(type, request.getQuantity() - storeItem.getQuantity());
        updatedStoreItems.add(inventory);
        storeItem.update(request, null, null);
        updatedStoreItems.add(storeItem);

        updatedStoreItems = storeItemRepository.saveAll(updatedStoreItems);
        return updatedStoreItems;
    }

    @Override
    public void delete(UUID id) {
        Optional<StoreItemModel> storeItemOptional = storeItemRepository.findById(id);
        if (storeItemOptional.isEmpty())
            throw new ExceptionResponse(InvalidInputError.STORE_ITEM_INVALID.getMessage(), InvalidInputError.STORE_ITEM_INVALID);
        StoreItemModel storeItem = storeItemOptional.get();
        Long productId = storeItem.getProductId();
        StoreModel store = storeItem.getStore();
        StoreItemType type = storeItem.getType();
        StoreItemModel inventory = storeItemRepository.findByStoreAndProductIdAndType(store, productId, StoreItemType.INVENTORY);

        if (type.equals(StoreItemType.EXPORT) && (inventory.getQuantity() < storeItem.getQuantity()))
            throw new ExceptionResponse(InvalidInputError.DELETE_EXPORT_STORE_ITEM_INVALID.getMessage(), InvalidInputError.DELETE_EXPORT_STORE_ITEM_INVALID);

        List<StoreItemModel> updatedStoreItems = new ArrayList<>();
        storeItem.setDeleted(true);
        updatedStoreItems.add(storeItem);
        inventory.setQuantity(inventory.getQuantity() - storeItem.getQuantity());
        updatedStoreItems.add(inventory);
        storeItemRepository.saveAll(updatedStoreItems);
    }

    @Override
    public PageResponse<StoreItemResponse> search(String product, String store, UUID order, StoreItemType type, Long fromTime, Long toTime, PageCriteria pageCriteria) {
        StoreItemQuery query = StoreItemQuery.builder()
                .product(convertSqlSearchText(product))
                .store(convertSqlSearchText(store))
                .order(order)
                .type(type)
                .fromTime(fromTime)
                .toTime(toTime)
                .build();
        Page<StoreItemModel> storeItemPage = storeItemRepository.search(query, pageCriteriaPageableMapper.toPageable(pageCriteria));
        List<StoreItemModel> storeItems = storeItemPage.getContent();
        Set<Long> productIds = storeItems.stream().filter(item -> item.getProductId() != null).map(StoreItemModel::getProductId).collect(Collectors.toSet());
        List<ProductModel> products = productRepository.findAllById(productIds);
        List<StoreItemResponse> responses = storeItemPage.getContent().stream()
                .map(item -> new StoreItemResponse(item, products))
                .collect(Collectors.toList());
        return PageResponse.<StoreItemResponse>builder()
                .paging(Paging.builder().totalCount(storeItemPage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(responses)
                .build();
    }

    @Override
    public List<StoreItemModel> findByProduct(ProductModel product) {
        List<StoreItemModel> storeItems = storeItemRepository.findByProductIdAndType(product.getId(), StoreItemType.INVENTORY);
        return storeItems;
    }
}

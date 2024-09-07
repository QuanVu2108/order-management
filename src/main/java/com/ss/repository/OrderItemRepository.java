package com.ss.repository;

import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.model.ProductModel;
import com.ss.model.StoreModel;
import com.ss.repository.query.OrderItemQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemModel, UUID> {
    List<OrderItemModel> findByOrderModel(OrderModel orderModel);

    @Query("SELECT DISTINCT orderItem FROM OrderItemModel orderItem " +
            " LEFT JOIN orderItem.orderModel orderModel " +
            " LEFT JOIN orderItem.product product " +
            " LEFT JOIN orderItem.store store " +
            " WHERE 1=1 " +
            " AND (:#{#query.ids} is null or orderItem.id in :#{#query.ids}) " +
            " AND (:#{#query.orderItemCode} is null or (upper(orderItem.code) like :#{#query.orderItemCode})) " +
            " AND (:#{#query.productCode} is null or (upper(product.code) like :#{#query.productCode})) " +
            " AND (:#{#query.productNumber} is null or (upper(product.productNumber) like :#{#query.productNumber})) " +
            " AND (:#{#query.productIds} is null or product.id in :#{#query.productIds}) " +
            " AND (:#{#query.orderCode} is null or (upper(orderModel.code) like :#{#query.orderCode})) " +
            " AND (:#{#query.orderIds} is null or orderModel.id in :#{#query.orderIds}) " +
            " AND (:#{#query.store} is null or (upper(store.name) like :#{#query.store})) " +
            " AND (:#{#query.storeIds} is null or store.id in :#{#query.storeIds}) " +
            " AND (:#{#query.statuses} is null or orderItem.status in :#{#query.statuses}) " +
            " AND (:#{#query.orderStatus} is null or orderModel.status = :#{#query.orderStatus}) " +
            " ORDER BY orderItem.updatedAt DESC " +
            ""
    )
    Page<OrderItemModel> search(OrderItemQuery query, Pageable pageable);

    List<OrderItemModel> findByStatusIn(List<OrderItemStatus> statuses);

    @Query("SELECT DISTINCT orderItem FROM OrderItemModel orderItem " +
            " LEFT JOIN orderItem.orderModel orderModel " +
            " LEFT JOIN orderItem.product product " +
            " LEFT JOIN orderItem.store store " +
            " WHERE 1=1 " +
            " AND (:#{#query.ids} is null or orderItem.id in :#{#query.ids}) " +
            " AND (:#{#query.productCode} is null or (upper(product.code) like :#{#query.productCode})) " +
            " AND (:#{#query.productIds} is null or product.id in :#{#query.productIds}) " +
            " AND (:#{#query.orderCode} is null or (upper(orderModel.code) like :#{#query.orderCode})) " +
            " AND (:#{#query.orderIds} is null or orderModel.id in :#{#query.orderIds}) " +
            " AND (:#{#query.store} is null or (upper(store.name) like :#{#query.store})) " +
            " AND (:#{#query.storeIds} is null or store.id in :#{#query.storeIds}) " +
            " AND (:#{#query.statuses} is null or orderItem.status in :#{#query.statuses}) " +
            " AND (:#{#query.orderStatus} is null or orderModel.status = :#{#query.orderStatus}) " +
            " ORDER BY orderItem.updatedAt DESC " +
            ""
    )
    List<OrderItemModel> searchList(OrderItemQuery query);

    List<OrderItemModel> findByStatusInAndQuantityInCartGreaterThan(List<OrderItemStatus> list, Long quantityInCart);

    List<OrderItemModel> findByOrderModelIn(List<OrderModel> orders);

    @Query("SELECT distinct orderItem.product FROM OrderItemModel orderItem WHERE orderItem.orderModel in :orders and orderItem.deleted = false ")
    List<ProductModel> findProductsByOrders(List<OrderModel> orders);

    @Query("SELECT distinct orderItem.store FROM OrderItemModel orderItem WHERE orderItem.orderModel in :orders and orderItem.deleted = false ")
    List<StoreModel> findStoresByOrders(List<OrderModel> orders);

    List<OrderItemModel> findByProduct(ProductModel product);

    List<OrderItemModel> findByProductIn(Collection<ProductModel> checkingProducts);
}

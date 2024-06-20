package com.ss.repository;

import com.ss.enums.StoreItemType;
import com.ss.model.StoreItemModel;
import com.ss.model.StoreModel;
import com.ss.repository.query.StoreItemQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItemModel, UUID> {

    List<StoreItemModel> findByStoreAndType(StoreModel store, StoreItemType type);

    StoreItemModel findByStoreAndProductIdAndType(StoreModel store, Long productId, StoreItemType type);

    @Query("SELECT e FROM StoreItemModel e " +
            " LEFT JOIN ProductModel pro ON e.productId = pro.id " +
            " WHERE 1=1 " +
            " AND (:#{#query.product} is null or UPPER(pro.code) like :#{#query.product} or UPPER(pro.productNumber) like :#{#query.product}) " +
            " AND (:#{#query.productIds} is null or pro.id in :#{#query.productIds}) " +
            " AND (:#{#query.store} is null or UPPER(e.store.name) like :#{#query.store}) " +
            " AND (:#{#query.order} is null or e.order = :#{#query.order}) " +
            " AND (:#{#query.type} is null or e.type = :#{#query.type}) " +
            " AND (:#{#query.fromTime} is null or e.time >= :#{#query.fromTime}) " +
            " AND (:#{#query.toTime} is null or e.time <= :#{#query.toTime}) " +
            " ORDER BY e.updatedAt DESC "
    )
    Page<StoreItemModel> search(StoreItemQuery query, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.quantity), 0)  FROM StoreItemModel e " +
            " LEFT JOIN ProductModel pro ON e.productId = pro.id " +
            " WHERE 1=1 " +
            " AND (:#{#query.product} is null or UPPER(pro.code) like :#{#query.product} or UPPER(pro.productNumber) like :#{#query.product}) " +
            " AND (:#{#query.productIds} is null or pro.id in :#{#query.productIds}) " +
            " AND (:#{#query.store} is null or UPPER(e.store.name) like :#{#query.store}) " +
            " AND (:#{#query.order} is null or e.order = :#{#query.order}) " +
            " AND (:#{#query.type} is null or e.type = :#{#query.type}) " +
            " AND (:#{#query.fromTime} is null or e.time >= :#{#query.fromTime}) " +
            " AND (:#{#query.toTime} is null or e.time <= :#{#query.toTime}) "
    )
    Long countAllQuantity(StoreItemQuery query);

    List<StoreItemModel> findByProductIdAndType(Long productId, StoreItemType type);

    List<StoreItemModel> findByStoreInAndProductIdInAndType(Set<StoreModel> stores, Set<Long> productIds, StoreItemType storeItemType);
}

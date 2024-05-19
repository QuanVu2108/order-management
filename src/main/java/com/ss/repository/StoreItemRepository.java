package com.ss.repository;

import com.ss.enums.StoreItemType;
import com.ss.model.ProductModel;
import com.ss.model.StoreItemModel;
import com.ss.model.StoreModel;
import com.ss.repository.query.StoreItemQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItemModel, UUID> {

    List<StoreItemModel> findByStoreAndType(StoreModel store, StoreItemType type);

    StoreItemModel findByStoreAndProductAndType(StoreModel store, ProductModel product, StoreItemType type);

    @Query("SELECT e FROM StoreItemModel e " +
            " WHERE 1=1 " +
            " AND (:#{#query.product} is null or UPPER(e.product.code) like :#{#query.product}) " +
            " AND (:#{#query.store} is null or UPPER(e.store.name) like :#{#query.store}) " +
            " AND (:#{#query.order} is null or e.order = :#{#query.order}) " +
            " AND (:#{#query.type} is null or e.type = :#{#query.type}) " +
            " AND (:#{#query.fromTime} is null or e.time >= :#{#query.fromTime}) " +
            " AND (:#{#query.toTime} is null or e.time <= :#{#query.toTime}) " +
            " ORDER BY e.updatedAt DESC "
    )
    Page<StoreItemModel> search(StoreItemQuery query, Pageable pageable);

    List<StoreItemModel> findByProductAndType(ProductModel product, StoreItemType type);
}

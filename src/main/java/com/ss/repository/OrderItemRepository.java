package com.ss.repository;

import com.ss.enums.OrderItemStatus;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemModel, UUID> {
    List<OrderItemModel> findByOrderModel(OrderModel orderModel);

    @Query("SELECT e FROM OrderItemModel e " +
            " WHERE 1=1 " +
            " AND ( (COALESCE(:ids, NULL) IS NULL) or e.id in :ids) " +
            " AND ( (COALESCE(:warehouseId, NULL) IS NULL) or e.warehouseId = :warehouseId) " +
            " AND (:status is null or e.status = :status) " +
            " AND (:keyword is null or UPPER(e.name) like :keyword or UPPER(e.content) like :keyword)"
    )
    Page<OrderItemModel> searchItem(List<UUID> ids, UUID warehouseId, String keyword, OrderItemStatus status, Pageable pageable);
}

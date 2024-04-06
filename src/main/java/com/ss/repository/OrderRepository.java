package com.ss.repository;

import com.ss.model.OrderModel;
import com.ss.repository.query.OrderQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, UUID> {
    @Query("SELECT e FROM OrderModel e " +
            " WHERE 1=1 " +
            " AND (:#{#query.ids} is null or e.id in :#{#query.ids}) " +
            " AND (:#{#query.code} is null or UPPER(e.code) like :#{#query.code}) " +
            " AND (:#{#query.statues} is null or e.status in :#{#query.statuses}) " +
            " AND (:#{#query.fromDate} is null or e.date >= :#{#query.fromDate}) " +
            " AND (:#{#query.toDate} is null or e.date <= :#{#query.toDate}) " +
            " AND (:#{#query.createdUsers} is null or e.createdBy in :#{#query.createdUsers}) "
    )
    Page<OrderModel> search(OrderQuery query, Pageable pageable);

    @Query("SELECT e FROM OrderModel e " +
            " WHERE 1=1 " +
            " AND (:#{#query.ids} is null or e.id in :#{#query.ids}) " +
            " AND (:#{#query.code} is null or UPPER(e.code) like :#{#query.code}) " +
            " AND (:#{#query.statuses} is null or e.status in :#{#query.statuses}) " +
            " AND (:#{#query.fromDate} is null or e.date >= :#{#query.fromDate}) " +
            " AND (:#{#query.toDate} is null or e.date <= :#{#query.toDate}) " +
            " AND (:#{#query.createdUsers} is null or e.createdBy in :#{#query.createdUsers}) "
    )
    List<OrderModel> searchList(OrderQuery query);

    @Query(value = "SELECT count(*) FROM order_tbl o where o.date >= :date", nativeQuery = true)
    long countByDate(Long date);
}

package com.ss.repository;

import com.ss.enums.OrderStatus;
import com.ss.model.OrderModel;
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
            " AND (:status is null or e.status = :status  )" +
            " AND (:code is null or UPPER(e.code) like :code )" +
            " AND (:fromDate is null or e.date >= :fromDate )" +
            " AND (:toDate is null or e.date <= :toDate )" +
            " AND (:createdUsers is null or e.createdBy in :createdUsers )"
    )
    Page<OrderModel> search(String code, OrderStatus status, Long fromDate, Long toDate, List<String> createdUsers, Pageable pageable);

    @Query(value = "SELECT count(*) FROM order_tbl o where o.date >= :date", nativeQuery = true)
    long countByDate(Long date);
}

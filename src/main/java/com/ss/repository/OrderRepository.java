package com.ss.repository;

import com.ss.enums.OrderStatus;
import com.ss.model.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, UUID> {
    @Query("SELECT e FROM OrderModel e " +
            " WHERE 1=1 " +
            " AND (:status is null or e.status = :status  )" +
            " AND (:keyword is null or UPPER(e.title) like :keyword or UPPER(e.code) like :keyword or UPPER(e.content) like :keyword  )"
    )
    Page<OrderModel> search(String keyword, OrderStatus status, Pageable pageable);

}

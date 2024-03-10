package com.ss.repository;

import com.ss.model.WarehouseModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseModel, UUID> {
    Optional<WarehouseModel> findByCode(String code);

    @Query("SELECT e FROM WarehouseModel e " +
            " WHERE 1=1 " +
            " AND (:keyword is null or UPPER(e.code) like :keyword or UPPER(e.name) like :keyword or UPPER(e.phoneNumber) like :keyword or UPPER(e.address) like :keyword  )"
    )
    Page<WarehouseModel> search(String keyword, Pageable pageable);
}

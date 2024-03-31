package com.ss.repository;

import com.ss.model.PermissionMenuModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionMenuRepository extends JpaRepository<PermissionMenuModel, UUID> {
    List<PermissionMenuModel> findByName(String name);

    @Query("SELECT e FROM PermissionMenuModel e " +
            " WHERE 1=1 " +
            " AND (:name is null or UPPER(e.name) like :name ) " +
            ""
    )
    Page<PermissionMenuModel> search(String name, Pageable pageable);
}

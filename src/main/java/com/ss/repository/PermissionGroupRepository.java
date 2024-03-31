package com.ss.repository;

import com.ss.model.PermissionGroupModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionGroupRepository extends JpaRepository<PermissionGroupModel, UUID> {
    List<PermissionGroupModel> findByName(String groupName);

    @Query("SELECT e FROM PermissionGroupModel e " +
            " WHERE 1=1 " +
            " AND (:name is null or UPPER(e.name) like :name ) " +
            ""
    )
    Page<PermissionGroupModel> search(String name, Pageable pageable);
}

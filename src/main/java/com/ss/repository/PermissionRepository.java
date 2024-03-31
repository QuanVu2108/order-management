package com.ss.repository;

import com.ss.model.PermissionGroupModel;
import com.ss.model.PermissionMenuModel;
import com.ss.model.PermissionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionModel, UUID> {
    List<PermissionModel> findByPermissionGroupAndPermissionMenuIn(PermissionGroupModel permissionGroup, List<PermissionMenuModel> permissionMenus);

    List<PermissionModel> findByPermissionMenu(PermissionMenuModel permissionMenu);
}

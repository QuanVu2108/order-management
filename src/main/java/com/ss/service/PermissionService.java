package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.PermissionGroupRequest;
import com.ss.dto.request.PermissionMenuRequest;
import com.ss.dto.request.PermissionRequest;
import com.ss.dto.response.PermissionGroupResponse;
import com.ss.model.PermissionGroupModel;
import com.ss.model.PermissionMenuModel;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PermissionGroupResponse create(PermissionGroupRequest request);

    PermissionGroupResponse update(UUID id, String groupName);

    PermissionGroupResponse updatePermission(UUID id, List<PermissionRequest> permissionRequests);

    void delete(UUID id);

    PageResponse<PermissionGroupModel> search(String keyword, PageCriteria pageCriteria);

    PermissionGroupModel findById(UUID id);

    PermissionMenuModel createPermissionMenu(PermissionMenuRequest request);

    PermissionMenuModel updatePermissionMenu(UUID id, PermissionMenuRequest request);

    void deletePermissionMenu(UUID id);

    PageResponse<PermissionMenuModel> getPermissionMenu(String name, PageCriteria pageCriteria);
}

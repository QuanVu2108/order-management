package com.ss.service.Impl;

import com.ss.dto.request.PermissionGroupRequest;
import com.ss.dto.request.PermissionMenuRequest;
import com.ss.dto.request.PermissionRequest;
import com.ss.dto.response.PermissionGroupResponse;
import com.ss.exception.CustomException;
import com.ss.model.PermissionGroupModel;
import com.ss.model.PermissionMenuModel;
import com.ss.model.PermissionModel;
import com.ss.repository.PermissionGroupRepository;
import com.ss.repository.PermissionMenuRepository;
import com.ss.repository.PermissionRepository;
import com.ss.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.ss.util.StringUtil.convertSqlSearchText;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionMenuRepository permissionMenuRepository;

    @Override
    @Transactional
    public PermissionGroupResponse create(PermissionGroupRequest request) {
        List<PermissionGroupModel> permissionGroups = permissionGroupRepository.findByName(request.getGroupName());
        if (!permissionGroups.isEmpty())
            throw new CustomException("name of group is duplicated", HttpStatus.CONFLICT);
        PermissionGroupModel permissionGroup = new PermissionGroupModel(request.getGroupName());
        permissionGroup = permissionGroupRepository.save(permissionGroup);
        Set<PermissionModel> permissions = new HashSet<>();
        List<PermissionMenuModel> permissionMenus = permissionMenuRepository.findAll();
        for (int i = 0; i < permissionMenus.size(); i++) {
            PermissionMenuModel permissionMenu = permissionMenus.get(i);
            PermissionRequest permissionRequest = request.getPermissions().stream()
                    .filter(item -> item.getPermissionMenuId().equals(permissionMenu.getId()))
                    .findFirst().orElse(new PermissionRequest());
            PermissionModel permission = new PermissionModel(permissionRequest, permissionGroup, permissionMenu);
            permissions.add(permission);

        }
        permissions = new HashSet<>(permissionRepository.saveAll(permissions));
        permissionGroup.setPermissions(permissions);
        return new PermissionGroupResponse(permissionGroup, permissions);
    }

    @Override
    public PermissionGroupResponse update(UUID id, String groupName) {
        Optional<PermissionGroupModel> permissionGroupOptional = permissionGroupRepository.findById(id);
        if (permissionGroupOptional.isEmpty())
            throw new CustomException("permission group is not existed", HttpStatus.BAD_REQUEST);
        PermissionGroupModel permissionGroup = permissionGroupOptional.get();
        if (!permissionGroup.getName().equals(groupName)) {
            List<PermissionGroupModel> permissionGroups = permissionGroupRepository.findByName(groupName);
            if (!permissionGroups.isEmpty())
                throw new CustomException("name of group is duplicated", HttpStatus.CONFLICT);
        }
        permissionGroup.setName(groupName);
        permissionGroup = permissionGroupRepository.save(permissionGroup);

        Set<PermissionModel> permissions = permissionGroup.getPermissions();
        return new PermissionGroupResponse(permissionGroup, permissions);
    }

    @Override
    @Transactional
    public PermissionGroupResponse updatePermission(UUID id, List<PermissionRequest> permissionRequests) {
        Optional<PermissionGroupModel> permissionGroupOptional = permissionGroupRepository.findById(id);
        if (permissionGroupOptional.isEmpty())
            throw new CustomException("permission group is not existed", HttpStatus.BAD_REQUEST);
        PermissionGroupModel permissionGroup = permissionGroupOptional.get();
        List<PermissionMenuModel> permissionMenus = permissionMenuRepository.findAll();
        List<PermissionModel> existedPermissions = permissionRepository.findByPermissionGroupAndPermissionMenuIn(permissionGroup, permissionMenus);
        Set<PermissionModel> updatedPermissions = new HashSet<>();
        permissionMenus.forEach(permissionMenu -> {
            PermissionRequest permissionRequest = permissionRequests.stream()
                    .filter(item -> item.getPermissionMenuId().equals(permissionMenu.getId()))
                    .findFirst().orElse(null);
            PermissionModel updatedPermission = existedPermissions.stream()
                    .filter(item -> item.getPermissionGroup().equals(permissionGroup) && item.getPermissionMenu().equals(permissionMenu))
                    .findFirst().orElse(new PermissionModel(permissionGroup, permissionMenu));
            updatedPermission.update(permissionRequest);
            updatedPermissions.add(updatedPermission);
        });
        permissionRepository.saveAll(updatedPermissions);
        permissionGroup.setPermissions(updatedPermissions);
        permissionGroupRepository.save(permissionGroup);
        return new PermissionGroupResponse(permissionGroup, updatedPermissions);
    }

    @Override
    public void delete(UUID id) {
        Optional<PermissionGroupModel> permissionGroupOptional = permissionGroupRepository.findById(id);
        if (permissionGroupOptional.isEmpty())
            throw new CustomException("permission group is not existed", HttpStatus.BAD_REQUEST);
        PermissionGroupModel permissionGroup = permissionGroupOptional.get();
        permissionGroup.setDeleted(true);
        permissionGroupRepository.save(permissionGroup);
    }

    @Override
    public List<PermissionGroupModel> search(String keyword) {
        return permissionGroupRepository.findByNameLike(convertSqlSearchText(keyword));
    }

    @Override
    public PermissionGroupModel findById(UUID id) {
        Optional<PermissionGroupModel> permissionGroupOptional = permissionGroupRepository.findById(id);
        if (permissionGroupOptional.isEmpty())
            return null;
        return permissionGroupOptional.get();
    }

    @Override
    @Transactional
    public PermissionMenuModel createPermissionMenu(PermissionMenuRequest request) {
        List<PermissionMenuModel> permissionMenus = permissionMenuRepository.findByName(request.getName());
        if (!permissionMenus.isEmpty())
            throw new CustomException("name of permission menu is duplicated", HttpStatus.CONFLICT);
        PermissionMenuModel permissionMenu = new PermissionMenuModel();
        permissionMenu.update(request);
        permissionMenu = permissionMenuRepository.save(permissionMenu);
        List<PermissionGroupModel> permissionGroups = permissionGroupRepository.findAll();
        Set<PermissionModel> newPermissions = new HashSet<>();
        for (int i = 0; i < permissionGroups.size(); i++) {
            newPermissions.add(new PermissionModel(permissionGroups.get(i), permissionMenu));
        }
        permissionMenu.setPermissions(newPermissions);
        return permissionMenu;
    }

    @Override
    public PermissionMenuModel updatePermissionMenu(UUID id, PermissionMenuRequest request) {
        Optional<PermissionMenuModel> permissionMenuOptional = permissionMenuRepository.findById(id);
        if (permissionMenuOptional.isEmpty())
            throw new CustomException("permission group is not existed", HttpStatus.BAD_REQUEST);
        PermissionMenuModel permissionMenu = permissionMenuOptional.get();
        permissionMenu.update(request);
        permissionMenu = permissionMenuRepository.save(permissionMenu);
        return permissionMenu;
    }

    @Override
    public void deletePermissionMenu(UUID id) {
        Optional<PermissionMenuModel> permissionMenuOptional = permissionMenuRepository.findById(id);
        if (permissionMenuOptional.isEmpty())
            throw new CustomException("permission group is not existed", HttpStatus.BAD_REQUEST);
        PermissionMenuModel permissionMenu = permissionMenuOptional.get();
        List<PermissionModel> permissions = permissionRepository.findByPermissionMenu(permissionMenu);
        permissions.forEach(permission -> permission.setDeleted(true));
        permissionRepository.saveAll(permissions);
        permissionMenu.setDeleted(true);
        permissionMenuRepository.save(permissionMenu);
    }

    @Override
    public List<PermissionMenuModel> getPermissionMenu() {
        return permissionMenuRepository.findAll();
    }
}

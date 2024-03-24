package com.ss.controller;

import com.ss.dto.request.PermissionGroupRequest;
import com.ss.dto.request.PermissionMenuRequest;
import com.ss.dto.request.PermissionRequest;
import com.ss.dto.response.PermissionGroupResponse;
import com.ss.dto.response.ServiceResponse;
import com.ss.model.PermissionGroupModel;
import com.ss.model.PermissionMenuModel;
import com.ss.service.PermissionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/permission-group")
@AllArgsConstructor
public class PermissionGroupController {

    private final PermissionService permissionService;

    @PostMapping
    ServiceResponse<PermissionGroupResponse> create(@RequestBody @Valid PermissionGroupRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, permissionService.create(request));
    }

    @PutMapping("/{id}")
    ServiceResponse<PermissionGroupResponse> update(
            @PathVariable @Valid UUID id,
            @RequestParam(name = "groupName") String groupName) {
        return ServiceResponse.succeed(HttpStatus.OK, permissionService.update(id, groupName));
    }

    @PutMapping("/permission/{id}")
    ServiceResponse<PermissionGroupResponse> updatePermission(
            @PathVariable @Valid UUID id,
            @RequestBody @Valid List<PermissionRequest> permissionRequests) {
        return ServiceResponse.succeed(HttpStatus.OK, permissionService.updatePermission(id, permissionRequests));
    }

    @DeleteMapping("/{id}")
    ServiceResponse<Void> delete(@PathVariable @Valid UUID id) {
        permissionService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping
    ServiceResponse<List<PermissionGroupModel>> search(@RequestParam(name = "keyword", required = false) String keyword) {
        return ServiceResponse.succeed(HttpStatus.OK, permissionService.search(keyword));
    }

    @PostMapping("/permission-menu")
    ServiceResponse<PermissionMenuModel> createPermissionMenu(@RequestBody @Valid PermissionMenuRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, permissionService.createPermissionMenu(request));
    }

    @PutMapping("/permission-menu/{id}")
    ServiceResponse<PermissionMenuModel> updatePermissionMenu(@PathVariable @Valid UUID id,
                                                              @RequestBody @Valid PermissionMenuRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, permissionService.updatePermissionMenu(id, request));
    }

    @DeleteMapping("/permission-menu/{id}")
    ServiceResponse<Void> deletePermissionMenu(@PathVariable @Valid UUID id) {
        permissionService.deletePermissionMenu(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping("/permission-menu")
    ServiceResponse<List<PermissionMenuModel>> getPermissionMenu() {
        return ServiceResponse.succeed(HttpStatus.OK, permissionService.getPermissionMenu());
    }

}

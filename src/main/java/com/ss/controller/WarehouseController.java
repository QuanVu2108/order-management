package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.WarehouseRequest;
import com.ss.dto.response.ServiceResponse;
import com.ss.model.WarehouseModel;
import com.ss.service.WarehouseService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/warehouse")
@AllArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    ServiceResponse<WarehouseModel> create(@RequestBody @Valid WarehouseRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, warehouseService.create(request));
    }

    @PutMapping("/{id}")
    ServiceResponse<WarehouseModel> update(
            @PathVariable @Valid UUID id,
            @RequestBody @Valid WarehouseRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, warehouseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ServiceResponse<Void> delete(@PathVariable @Valid UUID id) {
        warehouseService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping
    PageResponse<WarehouseModel> search(
            @RequestParam(name = "keyword", required = false) String keyword,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, warehouseService.search(keyword, pageCriteria));
    }
}

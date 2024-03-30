package com.ss.controller;

import com.ss.dto.Store;
import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.ProductPropertyRequest;
import com.ss.dto.response.ServiceResponse;
import com.ss.enums.ProductPropertyType;
import com.ss.model.ProductPropertyModel;
import com.ss.service.ProductPropertyService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/product-property")
@AllArgsConstructor
public class ProductPropertyController {

    private final ProductPropertyService productPropertyService;

    @PostMapping
    ServiceResponse<ProductPropertyModel> create(@RequestBody @Valid ProductPropertyRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productPropertyService.create(request));
    }

    @PutMapping("/{id}")
    ServiceResponse<ProductPropertyModel> update(
            @PathVariable @Valid UUID id,
            @RequestBody @Valid ProductPropertyRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productPropertyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ServiceResponse<Void> delete(@PathVariable @Valid UUID id) {
        productPropertyService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping
    PageResponse<ProductPropertyModel> search(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "type", required = false) ProductPropertyType type,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, productPropertyService.search(code, name, type, pageCriteria));
    }
}

package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.ProductPropertyRequest;
import com.ss.dto.request.ProductRequest;
import com.ss.dto.response.ServiceResponse;
import com.ss.enums.ProductPropertyType;
import com.ss.model.ProductModel;
import com.ss.model.ProductPropertyModel;
import com.ss.service.ProductPropertyService;
import com.ss.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductController {

    @Autowired
    private final ProductPropertyService productPropertyService;

    @Autowired
    private final ProductService productService;

    @PostMapping
    ServiceResponse<ProductModel> create(@RequestBody @Valid ProductRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.create(request));
    }

    @PutMapping("/{id}")
    ServiceResponse<ProductModel> update(@PathVariable @Valid long id,
                                         @RequestBody @Valid ProductRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ServiceResponse<Void> delete(@PathVariable @Valid long id) {
        productService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping
    PageResponse<ProductModel> search(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "number", required = false) String number,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "size", required = false) String size,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, productService.search(code, number, name, category, brand, color, size, pageCriteria));
    }

    // property
    @PostMapping("/property")
    ServiceResponse<ProductPropertyModel> createProperty(@RequestBody @Valid ProductPropertyRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productPropertyService.create(request));
    }

    @PutMapping("/property/{id}")
    ServiceResponse<ProductPropertyModel> updateProperty(@PathVariable @Valid UUID id,
                                                         @RequestBody @Valid ProductPropertyRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productPropertyService.update(id, request));
    }

    @DeleteMapping("/property/{id}")
    ServiceResponse<Void> deleteProperty(@PathVariable @Valid UUID id) {
        productPropertyService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping("/property")
    PageResponse<ProductPropertyModel> searchProperty(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "type", required = false) ProductPropertyType type,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, productPropertyService.search(code, name, type, pageCriteria));
    }
}

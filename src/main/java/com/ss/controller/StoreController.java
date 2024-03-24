package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.Store;
import com.ss.dto.response.ServiceResponse;
import com.ss.model.StoreModel;
import com.ss.service.StoreService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/store")
@AllArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    ServiceResponse<StoreModel> create(@RequestBody @Valid Store request) {
        return ServiceResponse.succeed(HttpStatus.OK, storeService.create(request));
    }

    @PutMapping("/{id}")
    ServiceResponse<StoreModel> update(
            @PathVariable @Valid UUID id,
            @RequestBody @Valid Store request) {
        return ServiceResponse.succeed(HttpStatus.OK, storeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ServiceResponse<Void> delete(@PathVariable @Valid UUID id) {
        storeService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping
    PageResponse<StoreModel> search(
            @RequestParam(name = "keyword", required = false) String keyword,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, storeService.search(keyword, pageCriteria));
    }
}

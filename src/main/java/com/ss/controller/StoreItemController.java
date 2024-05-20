package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.StoreItemDetailRequest;
import com.ss.dto.request.StoreItemRequest;
import com.ss.dto.response.ServiceResponse;
import com.ss.dto.response.StoreItemResponse;
import com.ss.enums.StoreItemType;
import com.ss.model.StoreItemModel;
import com.ss.service.StoreItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/store-item")
@AllArgsConstructor
public class StoreItemController {

    private final StoreItemService storeItemService;

    @PostMapping
    ServiceResponse<List<StoreItemModel>> create(@RequestBody @Valid StoreItemRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, storeItemService.create(request));
    }

    @PutMapping("/{id}")
    ServiceResponse<List<StoreItemModel>> update(@PathVariable @Valid UUID id,
                                                 @RequestBody @Valid StoreItemDetailRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, storeItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ServiceResponse<Void> delete(@PathVariable @Valid UUID id) {
        storeItemService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping
    PageResponse<StoreItemResponse> search(
            @RequestParam(name = "product", required = false) String product,
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "order", required = false) UUID order,
            @RequestParam(name = "type", required = false) StoreItemType type,
            @RequestParam(name = "fromTime", required = false) Long fromTime,
            @RequestParam(name = "toTime", required = false) Long toTime,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, storeItemService.search(product, store, order, type, fromTime, toTime, pageCriteria));
    }

}

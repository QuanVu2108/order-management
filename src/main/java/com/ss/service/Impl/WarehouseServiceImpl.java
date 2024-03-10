package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.WarehouseRequest;
import com.ss.exception.ExceptionResponse;
import com.ss.model.WarehouseModel;
import com.ss.repository.WarehouseRepository;
import com.ss.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    @Override
    public WarehouseModel create(WarehouseRequest request) {
        Optional<WarehouseModel> warehouseOptional = warehouseRepository.findByCode(request.getCode());
        if (!warehouseOptional.isEmpty())
            throw new ExceptionResponse("warehouse is existed!!!");
        WarehouseModel warehouseModel = new WarehouseModel();
        warehouseModel.update(request);
        warehouseRepository.save(warehouseModel);
        return warehouseModel;
    }

    @Override
    public WarehouseModel update(UUID id, WarehouseRequest request) {
        Optional<WarehouseModel> warehouseOptional = warehouseRepository.findById(id);
        if (warehouseOptional.isEmpty())
            throw new ExceptionResponse("warehouse is existed!!!");
        WarehouseModel warehouse = warehouseOptional.get();
        warehouse.update(request);
        warehouseRepository.save(warehouse);
        return warehouse;
    }

    @Override
    public void delete(UUID id) {
        Optional<WarehouseModel> warehouseOptional = warehouseRepository.findById(id);
        if (warehouseOptional.isEmpty())
            throw new ExceptionResponse("warehouse is existed!!!");
        WarehouseModel warehouse = warehouseOptional.get();
        warehouse.setDeleted(true);
        warehouseRepository.save(warehouse);
    }

    @Override
    public PageResponse<WarehouseModel> search(String keyword, PageCriteria pageCriteria) {
        if (keyword != null)
            keyword = "%" + keyword.toUpperCase() + "%";
        Page<WarehouseModel> warehousePage = warehouseRepository.search(keyword, pageCriteriaPageableMapper.toPageable(pageCriteria));

        return PageResponse.<WarehouseModel>builder()
                .paging(Paging.builder().totalCount(warehousePage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(warehousePage.getContent())
                .build();
    }

    @Override
    public WarehouseModel findById(UUID id) {
        if (id == null)
            return null;
        Optional<WarehouseModel> warehouseOptional = warehouseRepository.findById(id);
        if (warehouseOptional.isEmpty())
            return null;
        return warehouseOptional.get();
    }
}

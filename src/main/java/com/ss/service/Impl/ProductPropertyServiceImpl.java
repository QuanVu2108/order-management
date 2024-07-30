package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.ProductPropertyRequest;
import com.ss.enums.ProductPropertyType;
import com.ss.exception.ExceptionResponse;
import com.ss.model.ProductModel;
import com.ss.model.ProductPropertyModel;
import com.ss.repository.ProductPropertyRepository;
import com.ss.repository.ProductRepository;
import com.ss.service.ProductPropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.ss.util.CommonUtil.convertSqlSearchText;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductPropertyServiceImpl implements ProductPropertyService {

    private final ProductPropertyRepository repository;

    private final ProductRepository productRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    @Override
    public ProductPropertyModel create(ProductPropertyRequest request) {
        List<ProductPropertyModel> properties = repository.findByName(request.getName());
        if (!properties.isEmpty())
            throw new ExceptionResponse(request.getType() + " is existed!!!");
        String code = genCode(request.getType());
        ProductPropertyModel property = new ProductPropertyModel(code, request.getType());
        property.update(request);
        property = repository.save(property);
        return property;
    }

    private String genCode(ProductPropertyType type) {
        long countProperty = repository.countAllByType(type.toString());
        return String.valueOf(countProperty);
    }

    @Override
    public ProductPropertyModel update(UUID id, ProductPropertyRequest request) {
        Optional<ProductPropertyModel> propertyOptional = repository.findById(id);
        if (propertyOptional.isEmpty())
            throw new ExceptionResponse("property is existed!!!");
        ProductPropertyModel property = propertyOptional.get();
        if (request.getType() != null && !property.getType().equals(request.getType()))
            throw new ExceptionResponse("property type is existed!!!");
        property.update(request);
        property = repository.save(property);
        return property;
    }

    @Override
    public void delete(UUID id) {
        Optional<ProductPropertyModel> propertyOptional = repository.findById(id);
        if (propertyOptional.isEmpty())
            throw new ExceptionResponse("property is existed!!!");
        ProductPropertyModel property = propertyOptional.get();
        if (property.getType() != null) {
            List<ProductModel> usingPropertyProducts = productRepository.findByBrandOrCategory(property, property);
            if (!usingPropertyProducts.isEmpty())
                throw new ExceptionResponse("exists product was using on this property");
        }
        property.setDeleted(true);
        repository.save(property);
    }

    @Override
    public PageResponse<ProductPropertyModel> search(String code, String name, ProductPropertyType type, PageCriteria pageCriteria) {
        Page<ProductPropertyModel> pages = repository.search(convertSqlSearchText(code), convertSqlSearchText(name), type, pageCriteriaPageableMapper.toPageable(pageCriteria));
        return PageResponse.<ProductPropertyModel>builder()
                .paging(Paging.builder().totalCount(pages.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(pages.getContent())
                .build();
    }
}

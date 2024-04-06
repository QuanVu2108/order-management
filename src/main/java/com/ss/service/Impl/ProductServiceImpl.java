package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.ProductRequest;
import com.ss.exception.ExceptionResponse;
import com.ss.model.FileModel;
import com.ss.model.ProductModel;
import com.ss.model.ProductPropertyModel;
import com.ss.repository.FileRepository;
import com.ss.repository.ProductPropertyRepository;
import com.ss.repository.ProductRepository;
import com.ss.repository.query.ProductQuery;
import com.ss.service.ProductService;
import com.ss.util.StorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.ss.util.StringUtil.convertSqlSearchText;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final ProductPropertyRepository propertyRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    private final StorageUtil storageUtil;

    private final FileRepository fileRepository;

    @Override
    public ProductModel create(ProductRequest request) {
        ProductModel product = new ProductModel();
        List<UUID> propertyIds = new ArrayList<>();
        if (request.getCategoryId() != null) propertyIds.add(request.getCategoryId());
        if (request.getBrandId() != null) propertyIds.add(request.getBrandId());
        List<ProductPropertyModel> properties = (propertyIds.isEmpty()) ? new ArrayList<>() : propertyRepository.findAllById(propertyIds);
        ProductPropertyModel category = properties.stream()
                .filter(item -> request.getCategoryId() != null && request.getCategoryId().equals(item.getId()))
                .findFirst().orElse(null);
        ProductPropertyModel brand = properties.stream()
                .filter(item -> request.getBrandId() != null && request.getBrandId().equals(item.getId()))
                .findFirst().orElse(null);
        product.update(request, category, brand);
        product = repository.save(product);
        return product;
    }

    @Override
    public ProductModel update(long id, ProductRequest request) {
        Optional<ProductModel> productOptional = repository.findById(id);
        if (productOptional.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        ProductModel product = productOptional.get();
        List<UUID> propertyIds = new ArrayList<>();
        if (request.getCategoryId() != null) propertyIds.add(request.getCategoryId());
        if (request.getBrandId() != null) propertyIds.add(request.getBrandId());
        List<ProductPropertyModel> properties = (propertyIds.isEmpty()) ? new ArrayList<>() : propertyRepository.findAllById(propertyIds);
        ProductPropertyModel category = properties.stream()
                .filter(item -> request.getCategoryId() != null && request.getCategoryId().equals(item.getId()))
                .findFirst().orElse(null);
        ProductPropertyModel brand = properties.stream()
                .filter(item -> request.getBrandId() != null && request.getBrandId().equals(item.getId()))
                .findFirst().orElse(null);
        product.update(request, category, brand);
        product = repository.save(product);
        return product;
    }

    @Override
    public void delete(long id) {
        Optional<ProductModel> productOptional = repository.findById(id);
        if (productOptional.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        ProductModel product = productOptional.get();
        product.setDeleted(true);
        repository.save(product);
    }

    @Override
    public PageResponse<ProductModel> search(String code, String number, String name, String category, String brand, String color, String size, PageCriteria pageCriteria) {
        ProductQuery query = ProductQuery.builder()
                .code(convertSqlSearchText(code))
                .number(convertSqlSearchText(number))
                .name(convertSqlSearchText(name))
                .category(convertSqlSearchText(category))
                .brand(convertSqlSearchText(brand))
                .color(convertSqlSearchText(color))
                .size(convertSqlSearchText(size))
                .build();
        Page<ProductModel> pages = repository.search(query, pageCriteriaPageableMapper.toPageable(pageCriteria));
        return PageResponse.<ProductModel>builder()
                .paging(Paging.builder().totalCount(pages.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(pages.getContent())
                .build();
    }

    @Override
    public ProductModel uploadImage(long id, MultipartFile[] fileRequests) {
        Optional<ProductModel> productOptional = repository.findById(id);
        if (productOptional.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        ProductModel product = productOptional.get();

        Set<FileModel> images = new HashSet<>();
        for (int i = 0; i < fileRequests.length; i++) {
            FileModel file = storageUtil.uploadFile(fileRequests[i]);
            file.setProduct(product);
            fileRepository.save(file);
            images.add(file);
        }
        Set<FileModel> existedImages = product.getImages();
        existedImages.addAll(images);
        product.setImages(existedImages);
        return product;
    }

    @Override
    public List<ProductModel> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        return repository.findAllById(ids);
    }
}

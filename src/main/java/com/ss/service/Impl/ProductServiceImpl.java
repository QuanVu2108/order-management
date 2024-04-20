package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.ProductRequest;
import com.ss.dto.response.ProductCheckImportResponse;
import com.ss.enums.excel.ProductCheckImportExcelTemplate;
import com.ss.exception.ExceptionResponse;
import com.ss.exception.http.DuplicatedError;
import com.ss.model.FileModel;
import com.ss.model.ProductModel;
import com.ss.model.ProductPropertyModel;
import com.ss.model.StoreModel;
import com.ss.repository.FileRepository;
import com.ss.repository.ProductPropertyRepository;
import com.ss.repository.ProductRepository;
import com.ss.repository.query.ProductQuery;
import com.ss.service.ProductService;
import com.ss.service.StoreService;
import com.ss.util.StorageUtil;
import com.ss.util.excel.ExcelTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.ss.util.StringUtil.convertSqlSearchText;
import static com.ss.util.excel.ExcelUtil.readUploadFileData;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final ProductPropertyRepository propertyRepository;

    private final StoreService storeService;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    private final StorageUtil storageUtil;

    private final FileRepository fileRepository;

    @Override
    public ProductModel create(ProductRequest request) {
        long invalidProductCodeCnt = repository.countByCode(request.getCode());
        if (invalidProductCodeCnt > 0)
            throw new ExceptionResponse(DuplicatedError.PRODUCT_CODE_DUPLICATED.getMessage(), DuplicatedError.PRODUCT_CODE_DUPLICATED);

        long invalidProductNumberCnt = repository.countByProductNumber(request.getProductNumber());
        if (invalidProductNumberCnt > 0)
            throw new ExceptionResponse(DuplicatedError.PRODUCT_NUMBER_DUPLICATED.getMessage(), DuplicatedError.PRODUCT_NUMBER_DUPLICATED);

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
        if (!product.getCode().equals(request.getCode())) {
            long invalidProductCodeCnt = repository.countByCode(request.getCode());
            if (invalidProductCodeCnt > 0)
                throw new ExceptionResponse(DuplicatedError.PRODUCT_CODE_DUPLICATED.getMessage(), DuplicatedError.PRODUCT_CODE_DUPLICATED);
        }
        if (!product.getProductNumber().equals(request.getProductNumber())) {
            long invalidProductNumberCnt = repository.countByProductNumber(request.getProductNumber());
            if (invalidProductNumberCnt > 0)
                throw new ExceptionResponse(DuplicatedError.PRODUCT_NUMBER_DUPLICATED.getMessage(), DuplicatedError.PRODUCT_NUMBER_DUPLICATED);
        }

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


    @Override
    public List<ProductCheckImportResponse> checkImportFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        List<ProductCheckImportExcelTemplate> template = ProductCheckImportExcelTemplate.getColumns();
        List<ExcelTemplate> columns = template.stream().map(item -> new ExcelTemplate(item.getKey(), item.getColumn())).collect(Collectors.toList());

        List<ProductCheckImportResponse> responses = new ArrayList<>();
        Set<String> productNumbers = new HashSet<>();
        Set<String> productCodes = new HashSet<>();
        Set<String> storeNames = new HashSet<>();
        try {
            InputStream inputStream = file.getInputStream();
            List<Map<String, String>> assets = readUploadFileData(inputStream, fileName, columns, 1, 0, new ArrayList<>());
            int idx = 1;
            for (Map<String, String> asset : assets) {
                String productNumber = asset.get(ProductCheckImportExcelTemplate.NUMBER.getKey());
                String productCode = asset.get(ProductCheckImportExcelTemplate.CODE.getKey());
                String storeName = asset.get(ProductCheckImportExcelTemplate.STORE.getKey());
                boolean isValidNumber = true;
                Long quantity = null;
                Double cost = null;
                Double incentive = null;
                try {
                    quantity = Long.parseLong(asset.get(ProductCheckImportExcelTemplate.QUANTITY.getKey()));
                    cost = Double.parseDouble(asset.get(ProductCheckImportExcelTemplate.COST.getKey()));
                    incentive = Double.parseDouble(asset.get(ProductCheckImportExcelTemplate.INCENTIVE.getKey()));
                } catch (Exception ex) {
                    log.error("************ can not compare number!!!");
                    isValidNumber = false;
                }
                if (isValidNumber) {
                    boolean isValidProduct = true;
                    if (!StringUtils.hasText(productNumber) && !StringUtils.hasText(productCode))
                        isValidProduct = false;
                    if (isValidProduct) {
                        if (StringUtils.hasText(productNumber))
                            productNumbers.add(productNumber);
                        if (StringUtils.hasText(productCode))
                            productCodes.add(productCode);
                        storeNames.add(storeName);
                        ProductCheckImportResponse response = new ProductCheckImportResponse(idx++, productCode, productNumber, storeName, quantity, cost, incentive);
                        responses.add(response);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExceptionResponse("import file unsuccessfully!!! ");
        }
        List<ProductModel> products = repository.findByCodeInOrProductNumberIn(new ArrayList<>(productCodes), new ArrayList<>(productNumbers));
        List<StoreModel> stores = storeService.findByNameIn(storeNames);
        for (int i = 0; i < responses.size(); i++) {
            ProductCheckImportResponse response = responses.get(i);
            ProductModel product = null;
            if (StringUtils.hasText(response.getNumber())) {
                product = products.stream()
                        .filter(item -> item.getProductNumber() != null && item.getProductNumber().equals(response.getNumber()))
                        .findFirst().orElse(null);
            }
            if (product == null) {
                product = products.stream()
                        .filter(item -> item.getCode() != null && item.getCode().equals(response.getCode()))
                        .findFirst().orElse(null);
            }
            if (product == null)
                continue;
            response.setProduct(product);
            StoreModel store = stores.stream()
                    .filter(item -> item.getName().equals(response.getStoreName()))
                    .findFirst().orElse(null);
            response.setStore(store);
        }
        return responses;
    }

    @Override
    public ProductModel getByNumber(String number) {
        if (!StringUtils.hasText(number))
            return null;
        Optional<ProductModel> product = repository.findByProductNumber(number);
        if (product.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        return product.get();
    }
}

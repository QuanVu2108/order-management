package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.ProductRequest;
import com.ss.dto.response.FileResponse;
import com.ss.dto.response.ProductCheckImportResponse;
import com.ss.dto.response.ProductResponse;
import com.ss.dto.response.ProductSaleResponse;
import com.ss.enums.ProductPropertyType;
import com.ss.enums.StoreItemType;
import com.ss.enums.excel.*;
import com.ss.exception.ExceptionResponse;
import com.ss.exception.http.DuplicatedError;
import com.ss.exception.http.InvalidInputError;
import com.ss.model.*;
import com.ss.repository.*;
import com.ss.repository.query.ProductQuery;
import com.ss.service.*;
import com.ss.util.StorageUtil;
import com.ss.util.excel.ExcelTemplate;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.ss.enums.Const.DATE_TIME_DETAIL_FORMATTER;
import static com.ss.enums.Const.MAX_EXPORT_SIZE;
import static com.ss.util.DateUtils.instantToString;
import static com.ss.util.QRCodeUtil.generateQRCodeImage;
import static com.ss.util.StringUtil.*;
import static com.ss.util.excel.ExcelUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final ProductPropertyRepository propertyRepository;

    private final StoreService storeService;

    private final UserService userService;

    private final AsyncService asyncService;

    private final StoreItemRepository storeItemRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    private final StorageUtil storageUtil;

    private final FileRepository fileRepository;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    @Data
    private static class ProductImport {
        String productNumber;
        String code;
        String name;
        String categoryName;
        String brandName;
        String color;
        String size;
        Long soldPrice;
        Long costPrice;
        Long incentive;
        ProductPropertyModel category;
        ProductPropertyModel brand;
    }

    @Override
    public ProductResponse create(ProductRequest request) {
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
        product.setQrCode(generateQRCodeImage(String.valueOf(product.getId())));
        product = repository.save(product);
        return enrichProductResponse(Arrays.asList(product)).get(0);
    }


    @Override
    public ProductResponse update(long id, ProductRequest request) {
        Optional<ProductModel> productOptional = repository.findById(id);
        if (productOptional.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        ProductModel product = productOptional.get();
        if (!product.getCode().equals(request.getCode())) {
            long invalidProductCodeCnt = repository.countByCode(request.getCode());
            if (invalidProductCodeCnt > 0)
                throw new ExceptionResponse(DuplicatedError.PRODUCT_CODE_DUPLICATED.getMessage(), DuplicatedError.PRODUCT_CODE_DUPLICATED);
        }
        if (product.getProductNumber() != null && !product.getProductNumber().equals(request.getProductNumber())) {
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

        if (request.getImageIds() != null) {
            List<UUID> deletedImageIds = new ArrayList<>();
            Set<FileModel> existedImages = product.getImages();
            List<UUID> existedImageIds = existedImages.stream().map(FileModel::getId).collect(Collectors.toList());
            existedImageIds.forEach(existedImageId -> {
                if (!request.getImageIds().contains(existedImageId))
                    deletedImageIds.add(existedImageId);
            });
            if (!deletedImageIds.isEmpty()) {
                fileRepository.deleteAllById(deletedImageIds);
                Set<FileModel> newImages = new HashSet<>();
                existedImages.forEach(image -> {
                    if (!deletedImageIds.contains(image.getId()))
                        newImages.add(image);
                });
                product.setImages(newImages);
            }
        }

        product = repository.save(product);
        return enrichProductResponse(Arrays.asList(product)).get(0);
    }

    @Override
    public void delete(long id) {
        Optional<ProductModel> productOptional = repository.findById(id);
        if (productOptional.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        ProductModel product = productOptional.get();
        List<FileModel> files = fileRepository.findByProduct(product);
        files.forEach(file -> file.setDeleted(true));
        fileRepository.saveAll(files);
        product.setDeleted(true);
        repository.save(product);
    }

    @Override
    @Async
    public void importFile(MultipartFile fileRequest) {
        String fileName = fileRequest.getOriginalFilename();
        List<ProductExcelTemplate> template = ProductExcelTemplate.getColumns();
        List<ExcelTemplate> columns = template.stream().map(item -> new ExcelTemplate(item.getKey(), item.getColumn())).collect(Collectors.toList());

        List<ProductImport> productImports = new ArrayList<>();
        Set<String> productNumbers = new HashSet<>();
        Set<String> productCodes = new HashSet<>();
        Set<String> categoryNames = new HashSet<>();
        Set<String> brandNames = new HashSet<>();

        Map<String, List<String>> fileImports = new HashMap<>();
        try {
            InputStream inputStream = fileRequest.getInputStream();
            List<Map<String, String>> assets = readUploadFileData(inputStream, fileName, columns, 1, 0, new ArrayList<>());
            for (Map<String, String> asset : assets) {
                boolean isValid = true;
                String productNumber = asset.get(ProductExcelTemplate.PRODUCT_NUMBER.getKey());
                if (!StringUtils.hasText(productNumber))
                    isValid = false;

                String code = asset.get(ProductExcelTemplate.CODE.getKey());
                if (!StringUtils.hasText(code))
                    isValid = false;

                String name = asset.get(ProductExcelTemplate.NAME.getKey());
                if (!StringUtils.hasText(name))
                    isValid = false;

                String categoryName = asset.get(ProductExcelTemplate.CATEGORY.getKey());
                if (!StringUtils.hasText(categoryName))
                    isValid = false;

                String brandName = asset.get(ProductExcelTemplate.BRAND.getKey());
                if (!StringUtils.hasText(brandName))
                    isValid = false;

                String color = asset.get(ProductExcelTemplate.COLOR.getKey());
                if (!StringUtils.hasText(color))
                    isValid = false;

                String size = asset.get(ProductExcelTemplate.SIZE.getKey());
                if (!StringUtils.hasText(size))
                    isValid = false;

                if (isValid) {
                    productNumbers.add(productNumber);
                    productCodes.add(code);
                    categoryNames.add(categoryName);
                    brandNames.add(brandName);

                    ProductImport productImport = new ProductImport();
                    productImport.setProductNumber(productNumber);
                    productImport.setCode(code);
                    productImport.setName(name);
                    productImport.setCategoryName(categoryName);
                    productImport.setBrandName(brandName);
                    productImport.setColor(color);
                    productImport.setSize(size);

                    Long soldPrice = Long.valueOf(0);
                    try {
                        soldPrice = Long.parseLong(asset.get(ProductExcelTemplate.SOLD_PRICE.getKey()));
                    } catch (Exception ex) {
                    }
                    productImport.setSoldPrice(soldPrice);

                    Long costPrice = Long.valueOf(0);
                    try {
                        costPrice = Long.parseLong(asset.get(ProductExcelTemplate.COST_PRICE.getKey()));
                    } catch (Exception ex) {
                    }
                    productImport.setCostPrice(costPrice);
                    productImports.add(productImport);

                    String productUrls = asset.get(ProductExcelTemplate.IMAGE_URL.getKey());
                    if (StringUtils.hasText(productUrls)) {
                        List<String> fileUrls = List.of(productUrls.split(","));
                        fileImports.put(productNumber, fileUrls);
                    }

                    if (productImports.size() > 0 && (productImports.size() % 200 == 0)) {
                        createProductByImport(productImports, fileImports, productNumbers, productCodes, categoryNames, brandNames);

                        productImports.clear();
                        fileImports.clear();
                        productNumbers.clear();
                        productCodes.clear();
                        categoryNames.clear();
                        brandNames.clear();
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExceptionResponse("import fileRequest unsuccessfully!!! ");
        }

        log.info("******************** import file successfully");
    }

    private void createProductByImport(List<ProductImport> productImports, Map<String, List<String>> fileImports, Set<String> productNumbers, Set<String> productCodes, Set<String> categoryNames, Set<String> brandNames) {
        log.info("******************** import file start");
        // category
        List<ProductPropertyModel> existedCategories = propertyRepository.findByTypeAndNames(ProductPropertyType.CATEGORY, categoryNames.stream().map(item -> item.toUpperCase()).collect(Collectors.toList()));
        List<String> existedCategoryNames = existedCategories.stream().map(item -> item.getName().toUpperCase()).collect(Collectors.toList());
        List<String> newCategoryNames = categoryNames.stream()
                .filter(item -> !existedCategoryNames.contains(item.toUpperCase()))
                .collect(Collectors.toList());
        long countCategory = propertyRepository.countAllByType(ProductPropertyType.CATEGORY.toString());
        List<ProductPropertyModel> newCategories = new ArrayList<>();
        for (int i = 0; i < newCategoryNames.size(); i++) {
            ProductPropertyModel newCategory = new ProductPropertyModel(String.valueOf(countCategory), ProductPropertyType.CATEGORY);
            newCategory.setName(newCategoryNames.get(i));
            newCategories.add(newCategory);
            countCategory++;
        }
        newCategories = propertyRepository.saveAll(newCategories);
        existedCategories.addAll(newCategories);

        // category
        List<ProductPropertyModel> existedBrands = propertyRepository.findByTypeAndNames(ProductPropertyType.BRAND, categoryNames.stream().map(item -> item.toUpperCase()).collect(Collectors.toList()));
        List<String> existedBrandNames = existedBrands.stream().map(item -> item.getName().toUpperCase()).collect(Collectors.toList());
        List<String> newBrandNames = brandNames.stream()
                .filter(item -> !existedBrandNames.contains(item.toUpperCase()))
                .collect(Collectors.toList());
        long countBrand = propertyRepository.countAllByType(ProductPropertyType.BRAND.toString());
        List<ProductPropertyModel> newBrands = new ArrayList<>();
        for (int i = 0; i < newBrandNames.size(); i++) {
            ProductPropertyModel newBrand = new ProductPropertyModel(String.valueOf(countBrand), ProductPropertyType.BRAND);
            newBrand.setName(newBrandNames.get(i));
            newBrands.add(newBrand);
            countBrand++;
        }
        newBrands = propertyRepository.saveAll(newBrands);
        existedBrands.addAll(newBrands);

        List<ProductModel> existedProducts = repository.findByCodeInOrProductNumberIn(new ArrayList<>(productCodes), new ArrayList<>(productNumbers));
        List<ProductModel> products = new ArrayList<>();
        productImports.forEach(productImport -> {
            ProductModel product = existedProducts.stream()
                    .filter(item -> item.getProductNumber().toUpperCase().equals(productImport.getCode().toUpperCase())
                            || item.getCode().toUpperCase().equals(productImport.getCode().toUpperCase()))
                    .findFirst().orElse(new ProductModel());
            product.setProductNumber(productImport.getProductNumber());
            product.setCode(productImport.getCode());
            product.setName(productImport.getName());
            product.setColor(productImport.getColor());
            product.setSize(productImport.getSize());
            product.setSoldPrice(productImport.getSoldPrice());
            product.setCostPrice(productImport.getCostPrice());
            product.setIncentive(productImport.getIncentive());

            ProductPropertyModel category = existedCategories.stream()
                    .filter(item -> item.getName().toUpperCase().equals(productImport.getCategoryName().toUpperCase()))
                    .findFirst().orElse(null);
            product.setCategory(category);

            ProductPropertyModel brand = existedBrands.stream()
                    .filter(item -> item.getName().toUpperCase().equals(productImport.getBrandName().toUpperCase()))
                    .findFirst().orElse(null);
            product.setBrand(brand);
            products.add(product);
        });
        List<ProductModel> updatedProducts = repository.saveAll(products);

        asyncService.generateQRCodeProduct(updatedProducts);

        asyncService.createImageProduct(updatedProducts, fileImports);
    }

    @Override
    public PageResponse<ProductResponse> search(String code, String number, String name, String category, String brand, String color, String size, PageCriteria pageCriteria) {
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
        List<ProductResponse> productResponses = enrichProductResponse(pages.getContent());
        return PageResponse.<ProductResponse>builder()
                .paging(Paging.builder().totalCount(pages.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(productResponses)
                .build();
    }

    @Override
    public List<ProductResponse> enrichProductResponse(List<ProductModel> products) {
        List<ProductResponse> productResponses = new ArrayList<>();
        List<ProductPropertyModel> allProperties = propertyRepository.findAll();

        List<FileModel> allImages = fileRepository.findByProductIn(products);

        products.forEach(product -> {
            ProductResponse response = new ProductResponse(product);
            Set<FileResponse> images = allImages.stream()
                    .filter(item -> item.getProduct().getId() == product.getId())
                    .map(FileResponse::new)
                    .collect(Collectors.toSet());
            response.setImages(images);

            response.setCategory(allProperties.stream()
                    .filter(item -> item.getId().equals(product.getCategory().getId()))
                    .findFirst().orElse(null));
            response.setBrand(allProperties.stream()
                    .filter(item -> item.getId().equals(product.getBrand().getId()))
                    .findFirst().orElse(null));

            productResponses.add(response);
        });
        return productResponses;
    }

    @Override
    public List<ProductResponse> getList(String code, String number, String name, String category, String brand, String color, String size) {
        ProductQuery query = ProductQuery.builder()
                .code(convertSqlSearchText(code))
                .number(convertSqlSearchText(number))
                .name(convertSqlSearchText(name))
                .category(convertSqlSearchText(category))
                .brand(convertSqlSearchText(brand))
                .color(convertSqlSearchText(color))
                .size(convertSqlSearchText(size))
                .build();
        List<ProductModel> products = repository.getList(query);
        return enrichProductResponse(products);
    }

    @Override
    public Resource export(String code, String number, String name, String category, String brand, String color, String size) {
        PageCriteria pageCriteria = PageCriteria.builder()
                .pageIndex(1)
                .pageSize(MAX_EXPORT_SIZE)
                .build();
        PageResponse<ProductResponse> productPage = search(code, number, name, category, brand, color, size, pageCriteria);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Workbook workbook = null;
        try {
            workbook = getWorkbook(null, "product.xlsx");
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            Sheet sheet = workbook.createSheet("product");
            int rowHeaderIndex = 0;
            makeHeader(workbook, sheet, rowHeaderIndex, ProductExportExcel.values());

            List<Map<String, String>> areaAssets = getAssets(productPage.getData());
            makeContent(workbook, sheet, rowHeaderIndex, areaAssets, ProductExportExcel.values());
            autoSizeColumns(sheet, ProductExcelTemplate.values().length);

            workbook.write(byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray().clone();
            return new ByteArrayResource(bytes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionResponse(InvalidInputError.EXPORT_FILE_FAILED.getMessage(), InvalidInputError.EXPORT_FILE_FAILED);
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private List<Map<String, String>> getAssets(List<ProductResponse> data) {
        List<Map<String, String>> result = new ArrayList<>();
        int count = 1;
        for (ProductResponse product : data) {
            Map<String, String> map = new HashMap<>();
            map.put(ProductExportExcel.STT.getKey(), String.valueOf(count++));
            map.put(ProductExportExcel.PRODUCT_NUMBER.getKey(), product.getProductNumber());
            String imageUrl = "";
            if (product.getImages() != null && !product.getImages().isEmpty())
                imageUrl = product.getImages().stream().map(FileResponse::getUrl).collect(Collectors.joining(","));
            map.put(ProductExportExcel.IMAGE_URL.getKey(), imageUrl);
            map.put(ProductExportExcel.CODE.getKey(), product.getCode());
            map.put(ProductExportExcel.NAME.getKey(), product.getName());
            map.put(ProductExportExcel.CATEGORY.getKey(), product.getCategory().getName());
            map.put(ProductExportExcel.BRAND.getKey(), product.getBrand().getName());
            map.put(ProductExportExcel.COLOR.getKey(), product.getColor());
            map.put(ProductExportExcel.SIZE.getKey(), product.getSize());
            map.put(ProductExportExcel.SOLD_PRICE.getKey(), convertToString(product.getSoldPrice()));
            map.put(ProductExportExcel.COST_PRICE.getKey(), convertToString(product.getCostPrice()));
            map.put(ProductExportExcel.INCENTIVE.getKey(), convertToString(product.getIncentive()));
            map.put(ProductExportExcel.UPDATED_TIME.getKey(), instantToString(DATE_TIME_DETAIL_FORMATTER, product.getUpdatedAt()));
            result.add(map);
        }
        return result;
    }

    @Override
    public ProductResponse uploadImage(long id, MultipartFile[] fileRequests) {
        Optional<ProductModel> productOptional = repository.findById(id);
        if (productOptional.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        ProductModel product = productOptional.get();

        Set<FileModel> images = new HashSet<>();
        for (int i = 0; i < fileRequests.length; i++) {
            FileModel file = storageUtil.uploadFile(fileRequests[i]);
            file.setProduct(product);
            file = fileRepository.save(file);
            images.add(file);
        }
        Set<FileModel> existedImages = product.getImages();
        existedImages.addAll(images);
        product.setImages(existedImages);
        return enrichProductResponse(List.of(product)).get(0);
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
        List<ProductCheckImportExcelTemplate> template = List.of(ProductCheckImportExcelTemplate.values());
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
                Long quantity = Long.valueOf(0);
                Double cost = Double.valueOf(0);
                Double incentive = Double.valueOf(0);
                try {
                    quantity = Long.parseLong(asset.get(ProductCheckImportExcelTemplate.QUANTITY.getKey()));
                } catch (Exception ex) {
                    log.error("************ can not parse quantity in row " + idx);
                }
                try {
                    cost = Double.parseDouble(asset.get(ProductCheckImportExcelTemplate.COST.getKey()));
                } catch (Exception ex) {
                    log.error("************ can not parse cost in row " + idx);
                }
                try {
                    incentive = Double.parseDouble(asset.get(ProductCheckImportExcelTemplate.INCENTIVE.getKey()));
                } catch (Exception ex) {
                    log.error("************ can not parse incentive in row " + idx);
                }
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
            response.setProduct(new ProductResponse(product));
            StoreModel store = stores.stream()
                    .filter(item -> item.getName().equals(response.getStoreName()))
                    .findFirst().orElse(null);
            response.setStore(store);
        }
        return responses;
    }

    @Override
    @Transactional
    public List<ProductCheckImportResponse> checkImportFileKiotviet(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        StoreModel store = null;
        if (fileName.endsWith(".xlsx")) {
            String storeName = fileName.substring(0, fileName.lastIndexOf(".xlsx"));
            UserModel user = userService.getUserInfo();
            if (!user.getRoles().contains(RoleModel.ROLE_ADMIN)) {
                Set<StoreModel> stores = user.getStores();
                store = stores.stream()
                        .filter(item -> item.getName().equals(storeName))
                        .findFirst().orElse(null);
            } else {
                List<StoreModel> stores = storeService.findByNameIn(Arrays.asList(storeName));
                store = stores.isEmpty() ? null : stores.get(0);
            }
        }
        if (store == null)
            throw new ExceptionResponse(InvalidInputError.STORE_INVALID.getMessage(), InvalidInputError.STORE_INVALID);

        List<ProductCheckImportKiotvietExcelTemplate> template = List.of(ProductCheckImportKiotvietExcelTemplate.values());
        List<ExcelTemplate> columns = template.stream().map(item -> new ExcelTemplate(item.getKey(), item.getColumn())).collect(Collectors.toList());

        Set<String> productNumbers = new HashSet<>();
        Map<String, Long> productQuantities = new HashMap<>();
        try {
            InputStream inputStream = file.getInputStream();
            List<Map<String, String>> assets = readUploadFileData(inputStream, fileName, columns, 1, 0, new ArrayList<>());
            int idx = 1;
            for (Map<String, String> asset : assets) {
                String productNumber = asset.get(ProductCheckImportKiotvietExcelTemplate.NUMBER.getKey());
                productNumber = productNumber.substring(0, findFirstLetterIndex(productNumber));
                if (StringUtils.hasText(productNumber)) {
                    productNumbers.add(productNumber);
                    Long checkQuantity = Long.valueOf(0);
                    try {
                        checkQuantity = Long.parseLong(asset.get(ProductCheckImportExcelTemplate.QUANTITY.getKey()));
                    } catch (Exception ex) {
                        log.error("************ can not parse quantity in row " + idx);
                    }
                    if (checkQuantity != 0) {
                        Long updatedQuantity = productQuantities.get(productNumber) == null ? 0 : productQuantities.get(productNumber);
                        productQuantities.put(productNumber, updatedQuantity + checkQuantity);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExceptionResponse("import file unsuccessfully!!! ");
        }

        return enrichProductCheckImport(new ArrayList<>(productNumbers), productQuantities, store);

    }

    private List<ProductCheckImportResponse> enrichProductCheckImport(List<String> productNumbers, Map<String, Long> productQuantities, StoreModel store) {
        List<ProductCheckImportResponse> responses = new ArrayList<>();
        List<ProductModel> products = repository.findByProductNumberIn(productNumbers);
        List<ProductResponse> productResponses = enrichProductResponse(products);
        for (int i = 0; i < productNumbers.size(); i++) {
            String productNumber = productNumbers.get(i);
            ProductModel product = products.stream()
                    .filter(item -> item.getProductNumber().equals(productNumber))
                    .findFirst().orElse(null);
            if (product == null)
                continue;
            ProductCheckImportResponse response = new ProductCheckImportResponse();
            ProductResponse productResponse = productResponses.stream()
                    .filter(item -> item.getId() == product.getId())
                    .findFirst().orElse(null);
            response.setProduct(productResponse);
            response.setQuantity(productQuantities.get(productNumber));
            response.setStore(store);
            responses.add(response);
        }
        return responses;
    }

    @Override
    public ProductResponse getByNumber(String number) {
        if (!StringUtils.hasText(number))
            return null;
        Optional<ProductModel> product = repository.findByProductNumber(number);
        if (product.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        return enrichProductResponse(List.of(product.get())).get(0);
    }

    @Override
    public void generateQRCode() {
        List<ProductModel> products = repository.findAll();
        asyncService.generateQRCodeProduct(products);
    }

    @Override
    @Transactional
    public ProductSaleResponse getProductInfoBySale(long id) {
        Optional<ProductModel> productOptional = repository.findById(id);
        if (productOptional.isEmpty())
            throw new ExceptionResponse("product is not existed!!!");
        ProductSaleResponse response = new ProductSaleResponse();
        ProductModel product = productOptional.get();
        response.setProduct(enrichProductResponse(Arrays.asList(product)).get(0));
        List<StoreItemModel> storeItems = storeItemRepository.findByProductIdAndType(id, StoreItemType.INVENTORY);
        UserModel user = userService.getUserInfo();
        Set<StoreModel> ownerStores = user.getStores();
        response.setStoreInfo(ownerStores, storeItems);
        return response;
    }

    @Override
    @Transactional
    public List<ProductCheckImportResponse> checkConfirmFile(UUID id, MultipartFile file) {
        Optional<OrderModel> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty())
            throw new ExceptionResponse("order is not existed!!!");
        OrderModel order = orderOptional.get();
        String orderCode = order.getCode();

        List<OrderItemModel> orderItems = orderItemRepository.findByOrderModel(order);
        List<ProductModel> allProducts = orderItemRepository.findProductsByOrders(Arrays.asList(order));
        List<ProductResponse> allProductResponses = enrichProductResponse(allProducts);

        String fileName = file.getOriginalFilename();
        List<StoreModel> stores = new ArrayList<>();
        UserModel user = userService.getUserInfo();
        if (!user.getRoles().contains(RoleModel.ROLE_ADMIN)) {
            stores.addAll(user.getStores());
        } else
            stores.addAll(storeService.findAll());

        if (stores.isEmpty())
            throw new ExceptionResponse("Need config store for this account!!!");

        List<ProductCheckConfirmTemplate> template = List.of(ProductCheckConfirmTemplate.values());
        List<ExcelTemplate> columns = template.stream().map(item -> new ExcelTemplate(item.getKey(), item.getColumn())).collect(Collectors.toList());

        List<ProductCheckImportResponse> responses = new ArrayList<>();
        try {
            InputStream inputStream = file.getInputStream();
            List<Map<String, String>> assets = readUploadFileData(inputStream, fileName, columns, 4, 0, new ArrayList<>());
            int idx = 1;
            for (Map<String, String> asset : assets) {
                String orderCodeRequest = asset.get(ProductCheckConfirmTemplate.ORDER_CODE.getKey());
                if (!orderCode.equalsIgnoreCase(orderCodeRequest))
                    throw new ExceptionResponse("order is invalid " + idx);

                String storeName = asset.get(ProductCheckConfirmTemplate.STORE.getKey());
                StoreModel store = stores.stream().filter(item -> storeName.equalsIgnoreCase(item.getName())).findFirst().orElse(null);
                if (store != null) {
                    String productNumber = asset.get(ProductCheckConfirmTemplate.NUMBER.getKey());
                    if (StringUtils.hasText(productNumber)) {
                        OrderItemModel orderItem = orderItems.stream()
                                .filter(item -> productNumber.equalsIgnoreCase(item.getProduct().getProductNumber()))
                                .findFirst().orElse(null);
                        if (orderItem != null) {
                            Long quantity = Long.valueOf(0);
                            try {
                                quantity = Long.parseLong(asset.get(ProductCheckConfirmTemplate.QUANTITY.getKey()));
                            } catch (Exception ex) {
                                log.error("************ can not parse quantity in row " + idx);
                            }
                            if (quantity != 0) {
                                if (quantity > orderItem.getQuantityOrder())
                                    quantity = orderItem.getQuantityOrder();

                                ProductCheckImportResponse response = new ProductCheckImportResponse();
                                ProductResponse productResponse = allProductResponses.stream()
                                        .filter(item -> item.getId() == orderItem.getProduct().getId())
                                        .findFirst().orElse(null);
                                response.setProduct(productResponse);
                                response.setQuantity(quantity);
                                response.setStore(store);
                                responses.add(response);
                            }
                        }
                    }
                }
                idx++;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExceptionResponse("import file unsuccessfully!!! ");
        }
        return responses;
    }
}
